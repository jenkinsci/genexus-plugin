/*
 * The MIT License
 *
 * Copyright 2018 GeneXus S.A..
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
package org.jenkinsci.plugins.genexus.server;

import com.genexus.gxserver.client.clients.RevisionsQuery;
import com.genexus.gxserver.client.clients.TeamWorkService2Client;
import com.genexus.gxserver.client.info.ActionInfo;
import com.genexus.gxserver.client.info.RevisionInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.TaskListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.genexus.helpers.XMLStreamWriterEx;

/**
 *
 * @author jlr
 *
 */
public class CreateLogTask {

    private static final long serialVersionUID = 2L;
    private static final Logger LOGGER = Logger.getLogger(CreateLogTask.class.getName());

    private final TaskListener listener;
    private final GXSConnection gxsConnection;
    private final File logFile;
    private final Date fromTimestamp;
    private final Date toTimestamp;
    private final boolean fromExcluding;

    public CreateLogTask(TaskListener listener, GXSConnection gxsConnection, File logFilePath) {
        this(listener, gxsConnection, logFilePath, null, null);
    }

    public CreateLogTask(TaskListener listener, GXSConnection gxsConnection, File logFilePath, Date fromTimestamp, Date toTimestamp) {
        this(listener, gxsConnection, logFilePath, fromTimestamp, toTimestamp, true);
    }

    public CreateLogTask(TaskListener listener, GXSConnection gxsConnection, File logFilePath, Date fromTimestamp, Date toTimestamp, boolean fromExcluding) {
        this.listener = listener;
        this.gxsConnection = gxsConnection;
        this.logFile = logFilePath;
        this.fromTimestamp = DateUtils.cloneIfNotNull(fromTimestamp);
        this.toTimestamp = DateUtils.cloneIfNotNull(toTimestamp);
        this.fromExcluding = fromExcluding;
    }

    /**
     * @return true if success. The logFile may contain an actual log or the
     * error info.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public Boolean execute() throws IOException, InterruptedException {
        listener.getLogger().println("Checking GeneXus Server history");

        boolean success;
        try {
            TeamWorkService2Client twClient = new TeamWorkService2Client(
                    gxsConnection.getServerURL(),
                    gxsConnection.getUserName(),
                    gxsConnection.getUserPassword()
            );

            RevisionsQuery query = new RevisionsQuery(twClient, gxsConnection.getKbName(), gxsConnection.getKbVersion(), actualFromTimestamp(), toTimestamp);
            success = writeLog(logFile, query);
        } catch (RuntimeException e) {
            listener.getLogger().println("Error checking history: " + e.getMessage());
            success = false;
        }

        return success;
    }

    private Date actualFromTimestamp() {
        if (!fromExcluding) {
            return fromTimestamp;
        }

        // returns a datetime 1 second later, so that the initial fromTimestamp is excluded
        return new Date(fromTimestamp.getTime() + 1 * 1000);
    }

    @SuppressFBWarnings(
            value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", "REC_CATCH_EXCEPTION"},
            justification = "False positives for try-with-resources"
    )
    private boolean writeLog(File file, Iterable<RevisionInfo> revisions) throws IOException {
        try (
                OutputStream stream = new FileOutputStream(file);
                XMLStreamWriterEx xmlWriter = XMLStreamWriterEx.newInstance(stream)) {

            try (AutoCloseable docTag = xmlWriter.startDocument()) {
                try (AutoCloseable logTag = xmlWriter.startElement("log")) {
                    for (RevisionInfo revision : revisions) {
                        writeRevision(xmlWriter, revision);
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error saving change log file", ex);
            throw new IOException("Error saving change log file", ex);
        }
    }

    @SuppressFBWarnings(
            value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"},
            justification = "False positives for try-with-resources"
    )
    private void writeRevision(XMLStreamWriterEx xmlWriter, RevisionInfo revision) throws Exception {
        try (AutoCloseable logEntryTag = xmlWriter.startElement("logentry")) {
            xmlWriter.writeAttribute("revision", Integer.toString(revision.id));
            xmlWriter.writeSimpleElement("author", revision.author);
            xmlWriter.writeSimpleElement("date", DateUtils.toUTCstring(revision.date));

            writeActions(xmlWriter, revision);

            xmlWriter.writeSimpleElement("msg", revision.comment);
        }
    }

    @SuppressFBWarnings(
            value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"},
            justification = "False positives for try-with-resources"
    )
    private void writeActions(XMLStreamWriterEx xmlWriter, RevisionInfo revision) throws Exception {
        try (AutoCloseable actions = xmlWriter.startElement("actions")) {
            for (ActionInfo action : revision.getActions()) {
                try (AutoCloseable actionTag = xmlWriter.startElement("action")) {
                    xmlWriter.writeAttribute("type", action.actionType.toString());
                    xmlWriter.writeSimpleElement("objectGuid", action.objectGuid.toString());
                    xmlWriter.writeSimpleElement("objectType", action.objectType);
                    xmlWriter.writeSimpleElement("objectTypeGuid", action.getObjectTypeGuid().toString());
                    xmlWriter.writeSimpleElement("objectName", action.objectName);
                    xmlWriter.writeSimpleElement("objectDescription", action.objectDescription);
                }
            }
        }
    }
}
