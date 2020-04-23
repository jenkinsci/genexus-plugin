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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import org.jenkinsci.plugins.genexus.GeneXusInstallation;
import org.jenkinsci.plugins.genexus.helpers.CredentialsHelper;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.scm.SCMStep;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 *
 * @author jlr
 * @author Acaceres1996
 */
public final class GeneXusServerStep extends SCMStep {

    private static final long serialVersionUID = 1L;

    // GX installation
    private String gxInstallationId;

    // GXserver connection data
    private final String serverURL;
    private final String credentialsId;

    // Server KB info
    private final String kbName;
    private String kbVersion;

    // Local KB info
    private String localKbPath;
    private String localKbVersion;

    // Local KB DB info
    private String kbDbServerInstance;
    private String kbDbCredentialsId;
    private String kbDbName = "";
    private boolean kbDbInSameFolder = true;

    @DataBoundConstructor
    public GeneXusServerStep(String serverURL, String credentialsId, String kbName) {
        this(
                serverURL, credentialsId, kbName, /*gxInstallationId*/ "", /*kbVersion*/ "",
                /*localKbPath*/ "", /*localKbVersion*/ "", /*kbDbServerInstance*/ "",
                /*kbDbCredentialsId*/ "", /*kbDbName*/ "", /*kbDbInSameFolder*/ true
        );
    }

    public GeneXusServerStep(String serverURL, String credentialsId, String kbName, String gxInstallationId,
            String kbVersion, String localKbPath, String localKbVersion, String kbDbServerInstance,
            String kbDbCredentialsId, String kbDbName, boolean kbDbInSameFolder) {
        this.gxInstallationId = gxInstallationId;
        this.serverURL = serverURL;
        this.credentialsId = credentialsId;

        this.kbName = kbName;
        this.kbVersion = kbVersion;

        this.localKbPath = localKbPath;
        this.localKbVersion = localKbVersion;

        this.kbDbServerInstance = kbDbServerInstance;
        this.kbDbCredentialsId = kbDbCredentialsId;
        setKbDbName(kbDbName);
        setKbDbInSameFolder(kbDbInSameFolder);
    }

    @Override
    protected SCM createSCM() {
        return new GeneXusServerSCM(gxInstallationId, serverURL, credentialsId, kbName, kbVersion, localKbPath,
                localKbVersion, kbDbServerInstance, kbDbCredentialsId, kbDbName, kbDbInSameFolder);
    }

    @Exported
    public String getGxInstallationId() {
        return gxInstallationId;
    }

    @DataBoundSetter
    public void setGxInstallationId(String gxInstallationId) {
        this.gxInstallationId = gxInstallationId;
    }

    @Exported
    public String getServerURL() {
        return serverURL;
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

    @DataBoundSetter
    public void setKbVersion(String kbVersion) {
        this.kbVersion = kbVersion;
    }

    @Exported
    public String getLocalKbPath() {
        return localKbPath;
    }

    @DataBoundSetter
    public void setLocalKbPath(String localKbPath) {
        this.localKbPath = localKbPath;
    }

    @Exported
    public String getLocalKbVersion() {
        return localKbVersion;
    }

    @DataBoundSetter
    public void setLocalKbVersion(String localKbVersion) {
        this.localKbVersion = localKbVersion;
    }

    @Exported
    public String getKbDbServerInstance() {
        return kbDbServerInstance;
    }

    @DataBoundSetter
    public void setKbDbServerInstance(String kbDbServerInstance) {
        this.kbDbServerInstance = kbDbServerInstance;
    }

    @Exported
    public String getKbDbCredentialsId() {
        return kbDbCredentialsId;
    }

    @DataBoundSetter
    public void setKbDbCredentialsId(String kbDbCredentialsId) {
        this.kbDbCredentialsId = kbDbCredentialsId;
    }

    @Exported
    public String getKbDbName() {
        return kbDbName;
    }

    @DataBoundSetter
    public void setKbDbName(String kbDbName) {
        this.kbDbName = kbDbName;
    }

    @Exported
    public boolean isKbDbInSameFolder() {
        return kbDbInSameFolder;
    }

    @DataBoundSetter
    public void setKbDbInSameFolder(boolean kbDbInSameFolder) {
        this.kbDbInSameFolder = kbDbInSameFolder;
    }

    @Extension
    public static class DescriptorImpl extends SCMStepDescriptor {

        @Override
        public String getFunctionName() {
            return "gxserver";
        }

        @Override
        public String getDisplayName() {
            return "Checks out (or updates) a GeneXus Knowledge Base from a GeneXus Server";
        }

        private static final Logger LOGGER = Logger.getLogger(GeneXusServerStep.class.getName());

        public ListBoxModel doFillGxInstallationIdItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("(Default)", "");
            for (GeneXusInstallation installation : GeneXusInstallation.getInstallations()) {
                items.add(installation.getName(), installation.getName());
            }
            return items;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId,
                @QueryParameter String serverURL) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (!userCanSelect(item)) {
                return result;
            }

            return CredentialsHelper.getCredentialsList(item, credentialsId, serverURL);
        }

        public ListBoxModel doFillKbDbCredentialsIdItems(@AncestorInPath Item item,
                @QueryParameter String kbDbCredentialsId, @QueryParameter String kbDbServerInstance) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (!userCanSelect(item)) {
                return result;
            }

            return CredentialsHelper.getCredentialsList(item, kbDbCredentialsId, null);
        }

        private Boolean userCanSelect(Item item) {
            if (item == null) {
                return Jenkins.get().hasPermission(Jenkins.ADMINISTER);
            }

            return (item.hasPermission(Item.EXTENDED_READ) || item.hasPermission(CredentialsProvider.USE_ITEM));
        }

        /**
         * Validate the value for a GeneXus Server connection.
         *
         * @param value URL of a GeneXus Server installation
         * @return a FormValidation of a specific kind (OK, ERROR, WARNING)
         */
        public FormValidation doCheckServerURL(@QueryParameter String value) {

            // repository URL is required
            String url = Util.fixEmptyAndTrim(value);
            if (url == null) {
                return FormValidation.error("Server URL is required");
            }

            // repository URL syntax
            try {
                new URL(url);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                return FormValidation.error("Invalid Server URL. " + ex.getMessage());
            }

            return FormValidation.ok();
        }

        /**
         * Validate the value for GeneXus Server credentials.
         *
         * @param item Item to which the credentials apply
         * @param value id of credentials to validate
         * @param serverURL URL of a GeneXus Server installation
         * @return a FormValidation of a specific kind (OK, ERROR, WARNING)
         */
        @RequirePOST
        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value,
                @QueryParameter String serverURL) {
            if (!userCanSelect(item)) {
                return FormValidation.ok();
            }

            if (value == null || value.isEmpty()) {
                return FormValidation.ok(); // pre v15 GXservers may allow using no credentials
            }

            String url = Util.fixEmptyAndTrim(serverURL);
            if (url == null) {
                return FormValidation.ok();
            }

            try {
                StandardUsernamePasswordCredentials credentials = CredentialsHelper.getUsernameCredentials(item, value,
                        url);
                if (credentials == null) {
                    return FormValidation.error("Cannot find currently selected credentials");
                }
                // TODO: additional checks
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                return FormValidation.error("Unable to access the repository");
            }

            return FormValidation.ok();
        }

        /**
         * Validate the value for a SQL Server Instance.
         *
         * @param value SQL Server instance
         * @return a FormValidation of a specific kind (OK, ERROR, WARNING)
         */
        public FormValidation doCheckKbDbServerInstance(@QueryParameter String value) {
            return FormValidation.ok();
        }

        /**
         * Validate the value for the SQL Server credentials.
         *
         * @param item Item to which the credentials apply
         * @param value id of credentials to validate
         * @param kBDbServerInstance SQL Server instance used for the KB
         * @return a FormValidation of a specific kind (OK, ERROR, WARNING)
         */
        @RequirePOST
        public FormValidation doCheckKbDbCredentialsId(@AncestorInPath Item item, @QueryParameter String value,
                @QueryParameter String kBDbServerInstance) {
            if (!userCanSelect(item)) {
                return FormValidation.ok();
            }

            if (value == null || value.isEmpty()) {
                return FormValidation.ok(); // pre v15 GXservers may allow using no credentials
            }

            try {
                StandardUsernamePasswordCredentials credentials = CredentialsHelper.getUsernameCredentials(item, value,
                        null);
                if (credentials == null) {
                    return FormValidation.error("Cannot find currently selected credentials");
                }
                // TODO: additional checks
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                return FormValidation.error("Unable to access the server");
            }

            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }

        @Override
        public Step newInstance(@Nullable StaplerRequest req, @Nonnull JSONObject formData) throws FormException {
            return super.newInstance(req, formData);
        }
    }
}
