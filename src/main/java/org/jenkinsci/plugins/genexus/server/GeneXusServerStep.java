package org.jenkinsci.plugins.genexus.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;

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

public final class GeneXusServerStep extends SCMStep {

    private final String gxInstallationId;
    private final String url;
    private final String gxCredentialsId;
    private final String repository;
    private final String kbVersion;
    private final String dbmsInstance;
    private final String kbDbName;
    private final boolean kbDbInSameFolder;
    private String localKbPath;
    private String localKbVersion;
    private String dbmsCredentialsId;

    @DataBoundConstructor
    public GeneXusServerStep(String gxInstallationId, String url, String gxCredentialsId, String repository,
            String kbVersion, String localKbVersion, String dbmsInstance, String kbDbName,
            boolean kbDbInSameFolder) {
        this.gxInstallationId = gxInstallationId;
        this.url = url;
        this.gxCredentialsId = gxCredentialsId;
        this.repository = repository;
        this.kbVersion = kbVersion;
        this.localKbVersion = localKbVersion;
        this.dbmsInstance = dbmsInstance;
        this.kbDbName = kbDbName;
        this.kbDbInSameFolder = kbDbInSameFolder;
    }

    @Override
    protected SCM createSCM() {
        return new GeneXusServerSCM(gxInstallationId, url, gxCredentialsId, repository, kbVersion, localKbPath,
                localKbVersion, dbmsInstance, dbmsCredentialsId, kbDbName, kbDbInSameFolder);
    }

    public String getGxInstallationId() {
        return gxInstallationId;
    }

    public String getUrl() {
        return url;
    }

    public String getGxCredentialsId() {
        return gxCredentialsId;
    }

    public String getRepository() {
        return repository;
    }

    public String getKbVersion() {
        return kbVersion;
    }

    public String getLocalKbPath() {
        return localKbPath;
    }

    @DataBoundSetter
    public void setLocalKbPath(String localKbPath) {
        this.localKbPath = localKbPath;
    }

    public String getLocalKbVersion() {
        return localKbVersion;
    }

    public String getDbmsInstance() {
        return dbmsInstance;
    }

    public String getDbmsCredentialsId() {
        return dbmsCredentialsId;
    }

    @DataBoundSetter
    public void setDbmsCredentialsId(String dbmsCredentialsId) {
        this.dbmsCredentialsId = dbmsCredentialsId;
    }

    public String getKbDbName() {
        return kbDbName;
    }

    public boolean isKbDbInSameFolder() {
        return kbDbInSameFolder;
    }

    @Extension
    public static class DescriptorImpl extends SCMStepDescriptor {

        @Override
        public String getFunctionName() {
            return "gxserver";
        }

        @Override
        public String getDisplayName() {
            return "GeneXus Server";
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

        public ListBoxModel doFillGxCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId,
                @QueryParameter String url) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (!userCanSelect(item)) {
                return result;
            }

            return CredentialsHelper.getCredentialsList(item, credentialsId, url);
        }

        public ListBoxModel doFillDbmsCredentialsIdItems(@AncestorInPath Item item,
                @QueryParameter String dbmsCredentialsId, @QueryParameter String dbmsInstance) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (!userCanSelect(item)) {
                return result;
            }

            return CredentialsHelper.getCredentialsList(item, dbmsCredentialsId, null);
        }

        private Boolean userCanSelect(Item item) {
            if (item == null) {
                return Jenkins.get().hasPermission(Jenkins.ADMINISTER);
            }

            return (item.hasPermission(Item.EXTENDED_READ) || item.hasPermission(CredentialsProvider.USE_ITEM));
        }

        public FormValidation doCheckUrl(@QueryParameter String value) {
            String url = Util.fixEmptyAndTrim(value);
            if (url == null) {
                return FormValidation.error("Server URL is required");
            }
            try {
                new URL(url);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                return FormValidation.error("Invalid Server URL. " + ex.getMessage());
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