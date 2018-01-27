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
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import jenkins.MasterToSlaveFileCallable;
import org.jenkinsci.plugins.genexus.helpers.TeamDevArgumentListBuilder;

/**
 *
 * @author jlr
 * 
 */
public class CreateLogTask  extends MasterToSlaveFileCallable<Boolean> {

    private final String gxPath;
    private final TaskListener listener;
    private final GXSConnection gxsConnection;
    private final File logFile;
    private final Date fromTimestamp;
    private final Date toTimestamp;

    public CreateLogTask(TaskListener listener, String gxPath, GXSConnection gxsConnection, File logFile) {
        this(listener, gxPath, gxsConnection, logFile, null, null);
    }

    public CreateLogTask(TaskListener listener, String gxPath, GXSConnection gxsConnection, File logFile, Date fromTimestamp, Date toTimestamp) {
        this.gxPath = gxPath;
        this.listener = listener;
        this.gxsConnection = gxsConnection;
        this.logFile = logFile;
        this.fromTimestamp = DateUtils.cloneIfNotNull(fromTimestamp);
        this.toTimestamp = DateUtils.cloneIfNotNull(toTimestamp);
    }

    /**
     * @return true if the changelog.txt was created
     * info.
     */
    @Override
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
        TeamDevArgumentListBuilder args = new TeamDevArgumentListBuilder(gxPath, gxsConnection, fromTimestamp, toTimestamp, /*fromExcluding=*/ true);

        listener.getLogger().println("About to get revision");
        listener.getLogger().println(args.toString());

        boolean created = false;
        try {
            ProcessBuilder procBuilder = new ProcessBuilder(args.toCommandArray());
            procBuilder.redirectErrorStream(true);
            procBuilder.redirectOutput(logFile);
            Process proc = procBuilder.start();
            int exitCode = proc.waitFor();
            created = (exitCode == 0);
        }
        catch (Exception e) {
            listener.getLogger().println("Could not create log file: "+e.getMessage());
        }
        
        return created;
    }
}
