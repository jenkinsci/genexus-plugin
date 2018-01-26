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
package com.genexus.gxserver;

import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import jenkins.MasterToSlaveFileCallable;

/**
 *
 * @author jlr
 *
 * Obtains the revision number of a remote KB up to a given timestamp.
 */
public class GetLastRevisionTask extends MasterToSlaveFileCallable<GXSInfo> {

    private final String gxPath;
    private final TaskListener listener;
    private final GXSConnection gxsConnection;
    private final Date fromTimestamp;
    private final Date toTimestamp;

    public GetLastRevisionTask(TaskListener listener, String gxPath, GXSConnection gxsConnection) {
        this(listener, gxPath, gxsConnection, null, null);
    }

    public GetLastRevisionTask(TaskListener listener, String gxPath, GXSConnection gxsConnection, Date fromTimestamp, Date toTimestamp) {
        this.gxPath = gxPath;
        this.listener = listener;
        this.gxsConnection = gxsConnection;
        this.fromTimestamp = DateUtils.cloneIfNotNull(fromTimestamp);
        this.toTimestamp = DateUtils.cloneIfNotNull(toTimestamp);
    }

    /**
     * @return null if the parsing somehow fails. Otherwise a GXserver revision
     * info.
     */
    @Override
    public GXSInfo invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {

        TeamDevArgumentListBuilder args = new TeamDevArgumentListBuilder(gxPath, gxsConnection, fromTimestamp, toTimestamp);

        listener.getLogger().println("About to get revision");
        listener.getLogger().println(args.toString());

        ProcessBuilder procBuilder = new ProcessBuilder(args.toCommandArray());
        procBuilder.redirectErrorStream(true);
        Process proc = procBuilder.start();

        return getLastRevisionInfo(proc);
    }

    private GXSInfo getLastRevisionInfo(Process proc) throws IOException {
        
        List<GXSChangeLogSet.LogEntry> logEntries = GXSChangeLogParser.parse(proc.getInputStream());
                
        if (logEntries.isEmpty())
            return new GXSInfo(gxsConnection, 0, new Date(0));
        
        // We are assuming revisions always come in descending order, so we
        // just take the first revision as the most recent one.
        GXSChangeLogSet.LogEntry lastRevision = logEntries.get(0);
        GXSInfo gxsInfo = new GXSInfo(gxsConnection, lastRevision.getRevision(), new Date(lastRevision.getTimestamp()));
        return gxsInfo;
    }

    private static final long serialVersionUID = 1L;
}
