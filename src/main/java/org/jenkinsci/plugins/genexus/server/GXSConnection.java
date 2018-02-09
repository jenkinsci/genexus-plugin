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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author jlr
 */
@ExportedBean
public class GXSConnection extends AbstractDescribableImpl<GXSConnection> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String serverURL;
    private final String credentialsId;
    private final String kbName;
    private final String kbVersion;

    private static final String DEFAULT_SERVER_URL = "https://sandbox.genexusserver.com/v15";

    @DataBoundConstructor
    public GXSConnection(String serverURL, String credentialsId, String kbName, String kbVersion) {
        this.serverURL = serverURL;
        this.credentialsId = credentialsId;
        this.kbName = kbName;
        this.kbVersion = kbVersion;
    }

    @Exported
    public String getServerURL() {
        return StringUtils.defaultIfBlank(serverURL, DEFAULT_SERVER_URL);
    }

    @Exported
    public String getCredentialsId() {
        return credentialsId;
    }

    @Exported
    public String getKbName() {
        return kbName;
    }

    @Exported
    public String getKbVersion() {
        return kbVersion;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<GXSConnection> {

        @Override
        public String getDisplayName() {
            return "GXserver connection";
        }
    }
}
