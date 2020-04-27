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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.msbuild.MsBuildInstallation;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.genexus.GeneXusInstallation;
import org.jenkinsci.plugins.genexus.builders.CommandBuilder;
import org.jenkinsci.plugins.genexus.helpers.CredentialsHelper;
import org.jenkinsci.plugins.genexus.helpers.MsBuildArgumentListBuilder;
import org.jenkinsci.plugins.genexus.helpers.MsBuildInstallationHelper;
import org.jenkinsci.plugins.genexus.helpers.ToolHelper;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 *
 * @author jlr
 * @author mmarsicano
 * @author Acaceres1996
 */
public class GeneXusServerSCM extends SCM implements Serializable {

    private static final long serialVersionUID = 1L;

    // GX installation
    private final String gxInstallationId;

    // GXserver connection data
    private final String serverURL;
    private final String credentialsId;

    // Server KB info
    private final String kbName;
    private final String kbVersion;

    // Local KB info
    // TODO: get localKbVersion to update by looking in the working copy
    // which one is conected to the 'kbVersion' in the server
    private final String localKbPath;
    private final String localKbVersion;
    
    // Local KB DB info    
    private final String kbDbServerInstance;
    private final String kbDbCredentialsId;
    private final String kbDbName;
    private boolean kbDbInSameFolder = true;

    @DataBoundConstructor
    public GeneXusServerSCM(String gxInstallationId, String serverURL, String credentialsId, String kbName,
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
        this.kbDbName = kbDbName;
        this.kbDbInSameFolder = kbDbInSameFolder;
    }

    @Exported
    public String getGxInstallationId() {
        return gxInstallationId;
    }

    private GeneXusInstallation getGeneXusInstallation() {
        return GeneXusInstallation.getInstallation(gxInstallationId);
    }

    private String getGxPath() {
        GeneXusInstallation installation = getGeneXusInstallation();
        if (installation != null) {
            return installation.getHome();
        }

        return "";
    }

    private String getMSBuildInstallationId() {
        GeneXusInstallation installation = getGeneXusInstallation();
        if (installation != null) {
            return installation.getMsBuildInstallationId();
        }

        return "";
    }

    private String getMsBuildPath() {
        MsBuildInstallation msbuildTool = MsBuildInstallationHelper.getInstallation(getMSBuildInstallationId());
        if (msbuildTool != null) {
            return msbuildTool.getHome();
        }

        return "";
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

    @Exported
    public String getLocalKbPath() {
        return localKbPath;
    }

    @Exported
    public String getLocalKbVersion() {
        return localKbVersion;
    }

    @Exported
    public String getKbDbServerInstance() {
        return kbDbServerInstance;
    }

    @Exported
    public String getKbDbCredentialsId() {
        return kbDbCredentialsId;
    }

    @Exported
    public String getKbDbName() {
        return kbDbName;
    }

    @Exported
    public boolean isKbDbInSameFolder() {
        return kbDbInSameFolder;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new GXSChangeLogParser();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public PollingResult compareRemoteRevisionWith(@Nonnull Job<?, ?> project, @Nullable Launcher launcher,
            @Nullable FilePath workspace, @Nonnull TaskListener listener, @Nonnull SCMRevisionState _baseline)
            throws IOException, InterruptedException {
        final GXSRevisionState baseline = getSafeBaseline(project, launcher, workspace, listener, _baseline);

        FilePath workingPath = workspace != null ? workspace : new FilePath(project.getRootDir());

        try {
            GXSConnection gxs = getGXSConnection(project);
            GXSInfo currentInfo = workingPath
                    .act(new GetLastRevisionTask(listener, gxs, baseline.getRevisionDate(), new Date()));
            GXSRevisionState currentState = new GXSRevisionState(currentInfo.revision, currentInfo.revisionDate);

            return new PollingResult(baseline, currentState,
                    currentState.getRevision() > baseline.getRevision() ? Change.SIGNIFICANT : Change.NONE);
        } catch (IOException | InterruptedException ex) {
            listener.error(ex.getMessage());
            return PollingResult.BUILD_NOW;
        }
    }

    @Nonnull
    private GXSRevisionState getSafeBaseline(@Nonnull Job<?, ?> project, @Nullable Launcher launcher,
            @Nullable FilePath workspace, @Nonnull TaskListener listener, @Nonnull SCMRevisionState _baseline)
            throws IOException, InterruptedException {
        GXSRevisionState baseline = null;
        if (_baseline instanceof GXSRevisionState) {
            baseline = (GXSRevisionState) _baseline;
        } else if (project.getLastBuild() != null) {
            baseline = (GXSRevisionState) calcRevisionsFromBuild(project.getLastBuild(),
                    launcher != null ? workspace : null, launcher, listener);
        }

        if (baseline == null) {
            baseline = GXSRevisionState.MIN_REVISION;
        }

        return baseline;
    }

    /**
     * Please consider using the non-static version
     * {@link #parseGxServerRevisionFile(Run)}!
     */
    static GXSRevisionState parseRevisionFile(Run<?, ?> build) throws IOException {
        return parseRevisionFile(build, true);
    }

    GXSRevisionState parseGxServerRevisionFile(Run<?, ?> build) throws IOException {
        return parseRevisionFile(build);
    }

    /**
     * Reads the revision file of the specified build (or the closest, if the
     * flag is so specified.)
     *
     * @param findClosest If true, this method will go back the build history
     * until it finds a revision file. A build may not have a revision file for
     * any number of reasons (such as failure, interruption, etc.)
     * @return a GXSRevisionState which includes a revision number and date
     */
    @Nonnull
    static GXSRevisionState parseRevisionFile(Run<?, ?> build, boolean findClosest) throws IOException {

        if (findClosest) {
            for (Run<?, ?> b = build; b != null; b = b.getPreviousBuild()) {
                if (getRevisionFile(b).exists()) {
                    build = b;
                    break;
                }
            }
        }

        File file = getRevisionFile(build);
        if (!file.exists()) // nothing to compare against
        {
            return GXSRevisionState.MIN_REVISION;
        }

        GXSInfo info = loadRevisionFile(file);
        return new GXSRevisionState(info.revision, info.revisionDate);
    }

    /**
     * Polling can happen on the master and does not require a workspace.
     */
    @Override
    public boolean requiresWorkspaceForPolling() {
        // Since polling is done through web service calls, we don't require a workspace
        return false;
    }

    @Override
    public void checkout(Run<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener,
            File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {

        // Ensures workspace is created
        workspace.mkdirs();

        listener.getLogger().println("Checking out (or updating) " + getKbName() + " from " + getServerURL());
        Date updateTimestamp = new Date();
        listener.getLogger().println("Using the following timestamp for revisions:" + updateTimestamp.toString());

        CommandBuilder builder = createCheckoutOrUpdateAction(workspace, build.getParent());

        // TODO: Add support for parameterized builds
        // hint: see how SubversionSCM.java uses EnvVarsUtils to override env variables
        // with values from build.getBuildVariables() and then passes the
        // overritten values to nested taks
        /*
         * EnvVars env = build.getEnvironment(listener); if (build instanceof
         * AbstractBuild) { EnvVarsUtils.overrideAll(env, ((AbstractBuild)
         * build).getBuildVariables()); }
         */
        // TODO: we should get the actual revision as an output from the checkout or
        // update
        // Meanwhile we resort to get the latest revision up to the current time
        if (!builder.perform(build, workspace, launcher, listener)) {
            listener.error("Checkout (or update) from GeneXus Server failed");
            throw new IOException("error executing checkout/update from GeneXus Server");
        }

        // Create new revision
        GXSConnection gxs = getGXSConnection(build.getParent());
        GXSInfo currentInfo = calcCurrentInfo(workspace, listener, gxs, baseline, updateTimestamp);
        saveRevisionFile(build, currentInfo);

        if (changelogFile != null) {
            calcChangeLog(build, workspace, changelogFile, baseline, listener, gxs, currentInfo);
        }
    }

    private GXSConnection getGXSConnection(Item context) {
        String userName = "";
        String userPassword = "";

        StandardUsernamePasswordCredentials upCredentials = getServerCredentials(context);
        if (upCredentials != null) {
            userName = upCredentials.getUsername();
            userPassword = upCredentials.getPassword().getPlainText();
        }
        GXSConnection gxs = new GXSConnection(getServerURL(), userName, userPassword, getKbName(), getKbVersion());
        return gxs;
    }

    private StandardUsernamePasswordCredentials getServerCredentials(Item context) {
        return CredentialsHelper.getUsernameCredentials(context, getCredentialsId(), getServerURL());
    }

    private StandardUsernamePasswordCredentials getKbDbCredentials(Item context) {
        return CredentialsHelper.getUsernameCredentials(context, getKbDbCredentialsId(), null);
    }

    private GXSInfo calcCurrentInfo(FilePath workspace, TaskListener listener, GXSConnection gxs,
            SCMRevisionState baseline, Date updateTimestamp) throws IOException, InterruptedException {
        Date minDate = new Date(0);

        // try asking for changes after the baseline
        if (baseline instanceof GXSRevisionState) {
            Date baseDate = ((GXSRevisionState) baseline).getRevisionDate();
            if (baseDate.after(minDate)) {
                // ask for changes at or after baseDate
                GXSInfo currentInfo = calcCurrentInfo(workspace, listener, gxs, baseDate, updateTimestamp);

                // Verify curentInfo is actually at or after baseDate
                // because if there are no changes in the asked range we will
                // get a {0, minDate} state.
                // That might happen if after building for revision #42
                // the server got back to when the last revision was #41
                // (for example by restoring from a backup)
                if (currentInfo.revisionDate.compareTo(baseDate) >= 0) {
                    return currentInfo;
                }
                listener.getLogger().println("Found no revision on or after base line " + baseline.toString()
                        + ". Server reverted to past state?");
            }
        }

        // fall back to the last resort (asking from all changes up to updateTimestamp
        return calcCurrentInfo(workspace, listener, gxs, minDate, updateTimestamp);
    }

    private GXSInfo calcCurrentInfo(FilePath workspace, TaskListener listener, GXSConnection gxs, Date minDate,
            Date maxDate) throws IOException, InterruptedException {
        return workspace.act(new GetLastRevisionTask(listener, gxs, minDate, maxDate));
    }

    /**
     * Called after checkout/update has finished to compute the changelog.
     */
    private void calcChangeLog(Run<?, ?> build, FilePath workspace, File changelogFile, SCMRevisionState baseline,
            TaskListener listener, GXSConnection gxs, GXSInfo currentInfo) throws IOException, InterruptedException {

        GXSRevisionState _baseline = getSafeBaseline(build, baseline);

        FilePath changelogFilePath = new FilePath(changelogFile);

        boolean created = false;
        if (currentInfo.revisionDate.after(_baseline.getRevisionDate())) {
            created = workspace.act(new CreateLogTask(listener, gxs, changelogFilePath, _baseline.getRevisionDate(),
                    currentInfo.revisionDate));
        }

        if (!created) {
            createEmptyChangeLog(changelogFile, listener, "log");
        }
    }

    private GXSRevisionState getSafeBaseline(Run<?, ?> build, SCMRevisionState baseline) throws IOException {
        GXSRevisionState _baseline = GXSRevisionState.MIN_REVISION;
        if (baseline instanceof GXSRevisionState) {
            _baseline = (GXSRevisionState) baseline;
        } else if (build != null) {
            // build is the current one, we are looking for a 'baseline'
            build = build.getPreviousBuild();
            if (build != null) {
                // parseRevisionFile() keeps going back looking for a previous
                // build with a revision file
                _baseline = parseRevisionFile(build, /* findClosest= */ true);
            }
        }

        return _baseline;
    }

    private static void saveRevisionFile(Run<?, ?> build, GXSInfo info) throws IOException {
        saveRevisionFile(getRevisionFile(build), info);
    }

    private static void saveRevisionFile(File file, GXSInfo info) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, info);
    }

    private static GXSInfo loadRevisionFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, GXSInfo.class);
    }

    private CommandBuilder createCheckoutOrUpdateAction(FilePath workspace, Item context) throws IOException, InterruptedException {
        if (!kbAlreadyExists(getWorkingDirectory(workspace))) {
            return createCheckoutAction(workspace, context);
        }

        return createUpdateAction(workspace, context);
    }

    private MsBuildArgumentListBuilder createBaseMsBuildArgs(FilePath workspace, Item context, String... targetNames) {
        MsBuildArgumentListBuilder msbArgs = new MsBuildArgumentListBuilder(getMsBuildFile());
        msbArgs.addTargets(targetNames);

        msbArgs.addProperty("GX_PROGRAM_DIR", getGxPath());

        StandardUsernamePasswordCredentials serverCredentials = getServerCredentials(context);
        if (serverCredentials != null) {
            msbArgs.addProperty("ServerUsername", serverCredentials.getUsername(), true);
            msbArgs.addProperty("ServerPassword", serverCredentials.getPassword().getPlainText(), true);
        }

        if (StringUtils.isNotBlank(getKbVersion())) {
            msbArgs.addProperty("ServerKbVersion", getKbVersion());
        }

        msbArgs.addProperty("WorkingDirectory", getWorkingDirectory(workspace));

        StandardUsernamePasswordCredentials kbDbCredentials = getKbDbCredentials(context);
        if (kbDbCredentials != null) {
            msbArgs.addProperty("DbaseUseIntegratedSecurity", false);
            msbArgs.addProperty("DbaseServerUsername", kbDbCredentials.getUsername(), true);
            msbArgs.addProperty("DbaseServerPassword", kbDbCredentials.getPassword().getPlainText(), true);
        }

        return msbArgs;
    }

    private CommandBuilder createUpdateAction(FilePath workspace, Item context) throws IOException, InterruptedException {
        MsBuildArgumentListBuilder msbArgs = createBaseMsBuildArgs(workspace, context, "Update");

        if (StringUtils.isNotBlank(getLocalKbVersion())) {
            msbArgs.addProperty("WorkingVersion", getLocalKbVersion());
        }

        return createMsBuildAction(workspace, msbArgs);
    }

    private CommandBuilder createCheckoutAction(FilePath workspace, Item context) throws IOException, InterruptedException {
        MsBuildArgumentListBuilder msbArgs = createBaseMsBuildArgs(workspace, context, "Checkout");

        msbArgs.addProperty("ServerUrl", getServerURL());
        msbArgs.addProperty("ServerKbAlias", getKbName());

        // TODO: Add support for including all versions on checkout
        msbArgs.addProperty("GetAllKbVersions", false);

        if (StringUtils.isNotBlank(getKbDbServerInstance())) {
            msbArgs.addProperty("DbaseServerInstance", getKbDbServerInstance());
        }

        msbArgs.addProperty("DbaseName", getSafeKbDbName(getKbName(), getKbDbName()));
        msbArgs.addProperty("CreateDbInKbFolder", isKbDbInSameFolder());

        return createMsBuildAction(workspace, msbArgs);
    }

    private FilePath getWorkingDirectory(FilePath workspace) {
        if (!StringUtils.isBlank(getLocalKbPath())) {
            return new FilePath(workspace, getLocalKbPath());
        }

        return workspace.child(getKbName());
    }

    private String getMsBuildFile() {
        final String teamDevMsBuildFile = "TeamDev.msbuild";
        Path teamDevPath = Paths.get(getGxPath(), teamDevMsBuildFile);
        return teamDevPath.toString();
    }

    private CommandBuilder createMsBuildAction(FilePath workspace, MsBuildArgumentListBuilder msbArgs) throws IOException, InterruptedException {
        String msbuildExePath = ToolHelper.getToolFullPath(workspace, getMsBuildPath(), "msbuild.exe");
        msbArgs.prepend(msbuildExePath);
        return new CommandBuilder(msbArgs);
    }

    private static String getSafeKbDbName(String kbName, String kbDbName) {
        if (StringUtils.isNotBlank(kbDbName)) {
            return kbDbName;
        }

        return "GX_KB_" + kbName + "_" + UUID.randomUUID().toString();
    }

    /**
     * Gets the file that stores the revision.
     *
     * @param build a build instance for which the revision file is requested
     * @return File that stores the revision
     */
    public static File getRevisionFile(Run<?, ?> build) {
        return new File(build.getRootDir(), "revision.txt");
    }

    @Override
    @Nonnull
    public SCMRevisionState calcRevisionsFromBuild(@Nonnull Run<?, ?> build, @Nullable FilePath workspace,
            @Nullable Launcher launcher, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        return parseRevisionFile(build, true);
    }

    private static final Logger LOGGER = Logger.getLogger(GeneXusServerSCM.class.getName());

    private boolean kbAlreadyExists(FilePath workingDirectory) {
        try {
            return !(workingDirectory.list(new WildcardFileFilter("*.gxw", IOCase.INSENSITIVE)).isEmpty());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(GeneXusServerSCM.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override public String getKey() {
        StringBuilder b = new StringBuilder("gxserver");
        b.append(' ').append(serverURL);
        b.append(',').append(kbName);
        if (!StringUtils.isBlank(kbVersion)) {
            b.append(',').append(kbVersion);
        }
        return b.toString();
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<GeneXusServerSCM> {

        public static final String DEFAULT_GENEXUS_PATH = "C:\\Program Files (x86)\\GeneXus\\GeneXus15";
        public static final String DEFAULT_SERVER_URL = "https://sandbox.genexusserver.com/v15";

        @Override
        public boolean isApplicable(Job project) {
            return true;
        }

        public DescriptorImpl() {
            super(GeneXusServerSCM.class, null);
            load();
        }

        @Override
        public SCM newInstance(@Nullable StaplerRequest req, @Nonnull JSONObject formData) throws FormException {
            return super.newInstance(req, formData);
        }

        @Override
        public String getDisplayName() {
            return "GeneXus Server";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // Save configuration
            save();
            return super.configure(req, formData);
        }

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
    }
}
