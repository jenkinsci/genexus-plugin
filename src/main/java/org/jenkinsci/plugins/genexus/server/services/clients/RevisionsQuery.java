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
package org.jenkinsci.plugins.genexus.server.services.clients;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.genexus.server.info.RevisionInfo;
import org.jenkinsci.plugins.genexus.server.info.RevisionList;

/**
 *
 * @author jlr
 */
public class RevisionsQuery implements Iterable<RevisionInfo> {

    private final TeamWorkService2Client twClient;

    private final String kbName;
    private final int versionId;
    private final String query;

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, int versionId) {
        this(twClient, kbName, versionId, "");
    }

    public RevisionsQuery(TeamWorkService2Client twClient, String kbName, int versionId, String query) {
        this.twClient = twClient;
        this.kbName = kbName;
        this.versionId = versionId;
        this.query = query;
    }

    @Override
    public Iterator<RevisionInfo> iterator() {
        return new RevisionsIterator();
    }

    private class RevisionsIterator implements Iterator<RevisionInfo> {

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
            RevisionList newPage = twClient.getRevisions(kbName, versionId, query, newPageNumber);
            if (newPage != null && newPage.size() > 0) {
                currentPageNumber = newPageNumber;
                currentPageList = newPage;
                currentRevision = -1;
            } else {
                gotLastPage = true;
            }
        }
    }
}
