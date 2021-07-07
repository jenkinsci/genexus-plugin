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

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.MasterToSlaveFileCallable;
import org.jenkinsci.plugins.genexus.helpers.XMLStreamWriterEx;
import org.jenkinsci.plugins.genexus.server.info.ActionInfo;
import org.jenkinsci.plugins.genexus.server.info.RevisionInfo;
import org.jenkinsci.plugins.genexus.server.clients.RevisionsQuery;
import org.jenkinsci.plugins.genexus.server.clients.TeamWorkService2Client;

/**
 *
 * @author jlr
 *
 */
public class CreateLogTask extends MasterToSlaveFileCallable<Boolean> {

    private static final long serialVersionUID = 2L;

    private final TaskListener listener;
    private final GXSConnection gxsConnection;
    private final FilePath logFile;
    private final Date fromTimestamp;
    private final Date toTimestamp;
    private final boolean fromExcluding;

    public CreateLogTask(TaskListener listener, GXSConnection gxsConnection, FilePath logFilePath) {
        this(listener, gxsConnection, logFilePath, null, null);
    }

    public CreateLogTask(TaskListener listener, GXSConnection gxsConnection, FilePath logFilePath, Date fromTimestamp, Date toTimestamp) {
        this(listener, gxsConnection, logFilePath, fromTimestamp, toTimestamp, true);
    }

    public CreateLogTask(TaskListener listener, GXSConnection gxsConnection, FilePath logFilePath, Date fromTimestamp, Date toTimestamp, boolean fromExcluding) {
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
     */
    @Override
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
        listener.getLogger().println("Checking GeneXus Server history");

        File localFile = logFile.isRemote()
                ? new File(ws, "tempLog.xml")
                : new File(logFile.getRemote());

        boolean success;
        try {
            TeamWorkService2Client twClient = new TeamWorkService2Client(
                    gxsConnection.getServerURL(),
                    gxsConnection.getUserName(),
                    gxsConnection.getUserPassword()
            );

            RevisionsQuery query = new RevisionsQuery(twClient, gxsConnection.getKbName(), gxsConnection.getKbVersion(), actualFromTimestamp(), toTimestamp);
            success = writeLog(localFile, query);
        } catch (RuntimeException e) {
            listener.getLogger().println("Error checking history: " + e.getMessage());
            success = false;
        }

        if (success && logFile.isRemote()) {
            try {
                logFile.copyFrom(new FilePath(localFile));
            } catch (IOException | InterruptedException e) {
                listener.getLogger().println(
                        MessageFormat.format("Error copying local logFile ({0}) to master node ({1}): {3}",
                                localFile.toPath(),
                                logFile.getRemote(),
                                e.getMessage()
                        )
                );
                success = false;
            }
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

    private boolean writeLog(File file, Iterable<RevisionInfo> revisions) throws IOException {
        try {
            XMLStreamWriterEx xmlWriter = XMLStreamWriterEx.newInstance(file);

            try (AutoCloseable docTag = xmlWriter.startDocument()) {
                try (AutoCloseable logTag = xmlWriter.startElement("log")) {
                    for (RevisionInfo revision : revisions) {
                        writeRevision(xmlWriter, revision);
                    }
                }
            }

            xmlWriter.close();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(CreateLogTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Error saving log file", ex);
        }
    }

    private void writeRevision(XMLStreamWriterEx xmlWriter, RevisionInfo revision) throws Exception {
        try (AutoCloseable logEntryTag = xmlWriter.startElement("logentry")) {
            xmlWriter.writeAttribute("revision", Integer.toString(revision.id));
            xmlWriter.writeSimpleElement("author", revision.author);
            xmlWriter.writeSimpleElement("date", DateUtils.toUTCstring(revision.date));

            writeActions(xmlWriter, revision);

            xmlWriter.writeSimpleElement("msg", revision.comment);
        }
    }

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
