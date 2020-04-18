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

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

public class GXSMsBuildExeArgs {
    private final String gxpath;
    private final StandardUsernamePasswordCredentials serverCredentials;
    private final String kbVersion;
    private final StandardUsernamePasswordCredentials kbDbCredentials;
    private final String serverUrl;
    private final String KbName;
    private final String KbDbServerInstance;
    private final String KbDbName;
    private final boolean KbDbInSameFolder;

    public GXSMsBuildExeArgs(String gxpath, StandardUsernamePasswordCredentials serverCredentials, String kbVersion,
            StandardUsernamePasswordCredentials kbDbCredentials, String serverUrl, String KbName,
            String KbDbServerInstance, String KbDbName, boolean KbDbInSameFolder) {
        this.gxpath = gxpath;
        this.serverCredentials = serverCredentials;
        this.kbVersion = kbVersion;
        this.kbDbCredentials = kbDbCredentials;
        this.serverUrl = serverUrl;
        this.KbName = KbName;
        this.KbDbServerInstance = KbDbServerInstance;
        this.KbDbName = KbDbName;
        this.KbDbInSameFolder = KbDbInSameFolder;
    }

    public boolean getKbDbInSameFolder() {
        return KbDbInSameFolder;
    }

    public String getKbDbName() {
        return KbDbName;
    }

    public String getKbDbServerInstance() {
        return KbDbServerInstance;
    }

    public String getKbName() {
        return KbName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public StandardUsernamePasswordCredentials getKbDbCredentials() {
        return kbDbCredentials;
    }

    public String getKbVersion() {
        return kbVersion;
    }

    public StandardUsernamePasswordCredentials getServerCredentials() {
        return serverCredentials;
    }

    public String getGxpath() {
        return gxpath;
    }

}