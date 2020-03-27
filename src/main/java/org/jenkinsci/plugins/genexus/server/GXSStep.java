package org.jenkinsci.plugins.genexus.server;

import org.jenkinsci.plugins.workflow.steps.scm.SCMStep;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.scm.SCM;

public final class GXSStep extends SCMStep {

    private final String gxInstallationId;
    private final String serverURL;
    private final String credentialsId;
    private final String kbName;
    private final String kbVersion;
    private final String localKbPath;
    private final String localKbVersion;
    private final String kbDbServerInstance;
    private final String kbDbCredentialsId;
    private final String kbDbName;
    private final boolean kbDbInSameFolder;

    @DataBoundConstructor
    public GXSStep(String gxInstallationId, String serverURL, String credentialsId, String kbName, String kbVersion,
            String localKbPath, String localKbVersion, String kbDbServerInstance, String kbDbCredentialsId,
            String kbDbName, boolean kbDbInSameFolder) {
        this.gxInstallationId = gxInstallationId;
        this.serverURL = serverURL;
        this.credentialsId = credentialsId;
        this.kbName = kbName;
        this.kbVersion = kbVersion;
        this.localKbPath = localKbPath;
        this.localKbVersion = localKbVersion;
        this.kbDbServerInstance = kbDbServerInstance;
        this.kbDbCredentialsId = kbDbCredentialsId;
        this.kbDbName = kbDbName;
        this.kbDbInSameFolder = kbDbInSameFolder;
    }

    @Override
    protected SCM createSCM() {
        return new GeneXusServerSCM(gxInstallationId, serverURL, credentialsId, kbName, kbVersion, localKbPath,
                localKbVersion, kbDbServerInstance, kbDbCredentialsId, kbDbName, kbDbInSameFolder);
    }

    public String getGxInstallationId() {
        return gxInstallationId;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getKbName() {
        return kbName;
    }

    public String getKbVersion() {
        return kbVersion;
    }

    public String getLocalKbPath() {
        return localKbPath;
    }

    public String getLocalKbVersion() {
        return localKbVersion;
    }

    public String getKbDbServerInstance() {
        return kbDbServerInstance;
    }

    public String getKbDbCredentialsId() {
        return kbDbCredentialsId;
    }

    public String getKbDbName() {
        return kbDbName;
    }

    public boolean isKbDbInSameFolder() {
        return kbDbInSameFolder;
    }

    public static class DescriptorImpl extends SCMStepDescriptor {

        @Override
        public String getFunctionName() {
            return "gxserver";
        }

        @Override
        public String getDisplayName() {
            return "GeneXus Server";
        }

    }
}