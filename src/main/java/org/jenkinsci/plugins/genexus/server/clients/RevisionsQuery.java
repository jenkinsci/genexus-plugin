/*
 * The MIT License
 *
 * Copyright 2020 GeneXus S.A..
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.genexus.server.clients;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.genexus.helpers.UTCDateTimeFormatter;
import org.jenkinsci.plugins.genexus.helpers.XmlHelper;
import org.jenkinsci.plugins.genexus.server.info.RevisionInfo;
import org.jenkinsci.plugins.genexus.server.info.RevisionList;
import org.jenkinsci.plugins.genexus.server.info.VersionInfo;

/**
 *
 * @author jlr
 */
public class RevisionsQuery implements Iterable<RevisionInfo> {

    private final TeamWorkService2Client twClient;

    private final String kbName;
    private String versionName;
    private int versionId;
    private final String query;
    private final Date minDate;
    private final Date maxDate;

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, String versionName) {
        this(twClient, kbName, versionName, null);
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, String versionName, Date minDate) {
        this(twClient, kbName, versionName, minDate, null);
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, String versionName, Date minDate, Date maxDate) {
        this(twClient, kbName, versionName, minDate, maxDate, "");
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, String versionName, Date minDate, Date maxDate, String query) {
        this(twClient, kbName, -1, minDate, maxDate, query);
        this.versionName = versionName;
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, int versionId) {
        this(twClient, kbName, versionId, null);
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, int versionId, Date minDate) {
        this(twClient, kbName, versionId, minDate, null);
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, int versionId, Date minDate, Date maxDate) {
        this(twClient, kbName, versionId, minDate, maxDate, "");
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, int versionId, Date minDate, Date maxDate, String query) {
        this.twClient = twClient;
        this.kbName = kbName;
        this.versionId = versionId;
        this.versionName = null;
        this.minDate = minDate != null ? (Date) minDate.clone() : null;
        this.maxDate = minDate != null ? (Date) maxDate.clone() : null;
        this.query = getQueryString(this.minDate, this.maxDate, query);
    }

    private static String PARM_SEPARATOR = " ";
    private static String OPERATION_PARM = "operation:";
    private static String COMMIT_OPERATION = "Commit";
    private static String FROM_DATE_PARM = "after:";
    private static String TO_DATE_PARM = "before:";

    private static String getQueryString(Date minDate, Date maxDate, String query) {
        DateFormat dateFormat = UTCDateTimeFormatter.getQueryFormat();

        StringBuilder qb = new StringBuilder();

        qb.append(OPERATION_PARM).append(COMMIT_OPERATION);

        if (minDate != null) {
            qb.append(PARM_SEPARATOR).append(FROM_DATE_PARM).append(dateFormat.format(minDate));
        }

        if (maxDate != null) {
            qb.append(PARM_SEPARATOR).append(TO_DATE_PARM).append(dateFormat.format(maxDate));
        }

        if (StringUtils.isNotEmpty(query)) {
            qb.append(PARM_SEPARATOR).append(query);
        }

        return qb.toString();
    }

    @Override
    public Iterator<RevisionInfo> iterator() {
        return new RevisionsIterator();
    }

    public RevisionInfo getFirstItem() {
        Iterator<RevisionInfo> it = iterator();
        return it.hasNext() ? it.next() : null;
    }

    public void writeToFile(File file) throws IOException, FileNotFoundException, JAXBException {
        RevisionList list = new RevisionList();
        for (RevisionInfo rev : this) {
            list.add(rev);
        }

        XmlHelper.writeXml(list, file);
    }

    private class RevisionsIterator implements Iterator<RevisionInfo> {

        private static final int REVISIONS_PAGE_SIZE = 50;

        private int currentPageNumber;
        private RevisionList currentPageList;
        private int currentRevision = -1;

        private boolean gotLastPage = false;

        public RevisionsIterator() {
            this.currentPageList = null;
            currentPageNumber = 0;
        }

        @Override
        public boolean hasNext() {
            if ( // didn't get a first page, or already iterated over the current one
                    (currentPageList == null || currentRevision + 1 >= currentPageList.size())
                    // and didn't already find out there are no more pages
                    && !gotLastPage) {
                try {
                    getNextPage();
                } catch (IOException ex) {
                    Logger.getLogger(TeamWorkService2Client.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex.getClass().getSimpleName() + " getting revisions from GXserver: " + ex.toString(), ex);
                }
            }

            return (currentPageList != null && currentRevision + 1 < currentPageList.size());
        }

        @Override
        public RevisionInfo next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("No more revisions in this iteration");
            }

            return currentPageList.get(++currentRevision);
        }

        private void getNextPage() throws IOException {
            int newPageNumber = currentPageNumber + 1;
            RevisionList newPage = twClient.getRevisions(kbName, getVersionId(), query, newPageNumber);
            if (newPage != null && newPage.size() > 0) {
                currentPageNumber = newPageNumber;
                currentPageList = newPage;
                gotLastPage = (newPage.size() < REVISIONS_PAGE_SIZE);

                currentRevision = -1;
            } else {
                gotLastPage = true;
            }
        }

        private int getVersionId() throws IOException {
            if (versionId < 0) {
                for (VersionInfo v : twClient.getVersions(kbName)) {
                    if ((StringUtils.isEmpty(versionName) && v.isTrunk) || v.name.compareToIgnoreCase(versionName) == 0) {
                        versionId = v.id;
                        break;
                    }
                }
            }
            return versionId;
        }
    }
}
