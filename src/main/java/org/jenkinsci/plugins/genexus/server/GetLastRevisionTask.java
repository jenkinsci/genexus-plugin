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

import hudson.model.TaskListener;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.genexus.gxserver.client.info.RevisionInfo;
import com.genexus.gxserver.client.clients.RevisionsQuery;
import com.genexus.gxserver.client.clients.TeamWorkService2Client;

/**
 *
 * @author jlr
 *
 * Obtains the revision number of a remote KB up to a given timestamp.
 */
public class GetLastRevisionTask {

    private final TaskListener listener;
    private final GXSConnection gxsConnection;
    private final Date fromTimestamp;
    private final Date toTimestamp;

    public GetLastRevisionTask(TaskListener listener, GXSConnection gxsConnection) {
        this(listener, gxsConnection, null, null);
    }

    public GetLastRevisionTask(TaskListener listener, GXSConnection gxsConnection, Date fromTimestamp, Date toTimestamp) {
        this.listener = listener;
        this.gxsConnection = gxsConnection;
        this.fromTimestamp = DateUtils.cloneIfNotNull(fromTimestamp);
        this.toTimestamp = DateUtils.cloneIfNotNull(toTimestamp);
    }

    /**
     * @return null if the parsing somehow fails. Otherwise a GXserver revision
     * info.
     */
    public GXSInfo execute() throws IOException, InterruptedException {
        return getLatestRevisionInfo();
    }

    private GXSInfo getLatestRevisionInfo() throws IOException {
        try {
            TeamWorkService2Client twClient = new TeamWorkService2Client(
                    gxsConnection.getServerURL(),
                    gxsConnection.getUserName(),
                    gxsConnection.getUserPassword()
            );

            RevisionsQuery query = new RevisionsQuery(twClient, gxsConnection.getKbName(), gxsConnection.getKbVersion(), fromTimestamp, toTimestamp);

            // We are assuming revisions always come in descending order, so we
            // just take the first revision as the most recent one.
            RevisionInfo latestRevision = query.getFirstItem();
            if (latestRevision == null) {
                return new GXSInfo(gxsConnection, 0, new Date(0));
            }

            return new GXSInfo(gxsConnection, latestRevision.id, latestRevision.date);
        } catch (IOException ex) {
            Logger.getLogger(GetLastRevisionTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Error checking for last revision", ex);
        }
    }

    private static final long serialVersionUID = 1L;
}
