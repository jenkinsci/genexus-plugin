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
import java.util.Date;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import jenkins.MasterToSlaveFileCallable;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

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
        
        File logFile = new File(ws, "scm-polling.txt");
        FilePath logFilePath = new FilePath(logFile);

        // we avoid excluding the fromTimestamp so that we get at least the
        // last known revision
        CreateLogTask createLogTask = new CreateLogTask(listener, gxPath, gxsConnection, logFilePath, fromTimestamp, toTimestamp, /* fromExcluding= */ false);
        if (!createLogTask.invoke(ws, channel)) {
            throw new IOException("Error checking for last revision");
        }

        return GetLastRevisionInfo(logFile);
    }

    private GXSInfo GetLastRevisionInfo(File logFile) throws IOException {
        // Use URI to avoid errors when the path contains accented characters
        // (eg: Ô ç á)
        String systemId = logFile.toURI().toString();
        listener.getLogger().println("Trying to get last revision info from " + systemId);
        InputSource inputSource = new InputSource(systemId);

        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            // We are assuming revisions always come in descending order, so we
            // just take the first revision as the most recent one.
            Node lastEntry = (Node) xpath.evaluate("/log/logentry[1]", inputSource, XPathConstants.NODE);
            if (lastEntry == null)
                return new GXSInfo(gxsConnection, 0, new Date(0));

            Number nRev = (Number) xpath.evaluate("@revision", lastEntry, XPathConstants.NUMBER);
            String utcDate = (String) xpath.evaluate("date", lastEntry, XPathConstants.STRING);
            return new GXSInfo(gxsConnection, nRev.intValue(), DateUtils.fromUTCstring(utcDate));
        } catch (XPathExpressionException ex) {
            throw new IOException("Error checking for last revision", ex);
        }
    }
    
    private static final long serialVersionUID = 1L;
}
