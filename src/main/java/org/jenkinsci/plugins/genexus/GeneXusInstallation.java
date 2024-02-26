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
package org.jenkinsci.plugins.genexus;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import static hudson.init.InitMilestone.EXTENSIONS_AUGMENTED;
import hudson.init.Initializer;
import hudson.model.Descriptor;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.msbuild.MsBuildInstallation;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 *
 * @author jlr
 */
public final class GeneXusInstallation extends ToolInstallation
        implements NodeSpecific<GeneXusInstallation>, EnvironmentSpecific<GeneXusInstallation> {

    /**
     * Constant <code>DEFAULT="Default"</code>
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "not needed on deserialization")
    public static transient final String DEFAULT = "Default";

    private static final long serialVersionUID = 1L;

    private final String msBuildInstallationId;

    public GeneXusInstallation(String name, String home, String msBuildInstallationId) {
        this(name, home, msBuildInstallationId, Collections.<ToolProperty<?>>emptyList());
    }

    /**
     *
     * @param name Installation name
     * @param home Path to GeneXus Installation
     * @param msBuildInstallationId MSBuild installation id to be used
     * @param properties Tool properties
     */
    @DataBoundConstructor
    public GeneXusInstallation(String name, String home, String msBuildInstallationId, List<? extends ToolProperty<?>> properties) {
        super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), properties);
        this.msBuildInstallationId = Util.fixEmptyAndTrim(msBuildInstallationId);
    }

    public String getMsBuildInstallationId() {
        return msBuildInstallationId;
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        String home = getHome();
        if (home == null) {
            return;
        }
        env.put("GX_PROGRAM_DIR", home);
    }

    /**
     * Returns the default installation.
     *
     * @return default installation
     */
    public static GeneXusInstallation getDefaultInstallation() {
        GeneXusInstallation tool = GeneXusInstallation.getInstallation(GeneXusInstallation.DEFAULT);
        if (tool != null) {
            return tool;
        }

        GeneXusInstallation[] installations = GeneXusInstallation.getInstallations();
        if (installations.length == 0) {
            onLoaded();
            installations = GeneXusInstallation.getInstallations();
        }

        if (installations.length > 0) {
            return installations[0];
        }

        return null;
    }

    @Override
    public GeneXusInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new GeneXusInstallation(getName(), translateFor(node, log), getMsBuildInstallationId());
    }

    @Override
    public GeneXusInstallation forEnvironment(EnvVars environment) {
        return new GeneXusInstallation(getName(), environment.expand(getHome()), getMsBuildInstallationId());
    }

    public String getExecutable(final GeneXusExecutable executable, Launcher launcher) throws IOException, InterruptedException {
        return getFilePath(executable.getName(launcher.isUnix()), launcher);
    }

    public String getFilePath(final String fileName, Launcher launcher) throws IOException, InterruptedException {
        VirtualChannel channel = launcher.getChannel();
        if (channel == null) {
            return null;
        }

        String gxHome = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
        return channel.call(new FileOnNodeValidator(gxHome, fileName));
    }

    public String getExecutable(Launcher launcher) throws IOException, InterruptedException {
        return getExecutable(GeneXusExecutable.GENEXUS, launcher);
    }

    public static GeneXusInstallation[] getInstallations() {
        DescriptorImpl descriptor = ToolInstallation.all().get(DescriptorImpl.class);
        if (descriptor == null) {
            return new GeneXusInstallation[]{};
        }

        return descriptor.getInstallations();
    }

    private static GeneXusInstallation getInstallation(String installationId) {
        if (installationId == null) {
            return null;
        }

        for (GeneXusInstallation i : getInstallations()) {
            if (installationId.equals(i.getName())) {
                return i;
            }
        }

        return null;
    }

    /**
     * Resolves GeneXus installation by name.
     *
     * @param installationId installation Id. If {@code null}, default
     * installation will be used (if exists)
     * @param builtOn Node for which the installation should be resolved Can be
     * {@link Jenkins#getInstance()} when running on controller
     * @param env Additional environment variables
     * @param listener Event listener
     * @return GeneXus installation or {@code null} if it cannot be resolved
     */
    @CheckForNull
    public static GeneXusInstallation resolveGeneXusInstallation(@CheckForNull String installationId,
            @CheckForNull Node builtOn,
            @CheckForNull EnvVars env,
            @NonNull TaskListener listener) {

        GeneXusInstallation gx = null;
        if (StringUtils.isNotBlank(installationId)) {
            gx = GeneXusInstallation.getInstallation(installationId);
        }

        if (gx == null) {
            listener.getLogger().println("Selected GeneXus installation does not exist. Using Default");
            gx = GeneXusInstallation.getDefaultInstallation();
        }

        if (gx != null) {
            if (builtOn != null) {
                try {
                    gx = gx.forNode(builtOn, listener);
                } catch (IOException | InterruptedException e) {
                    listener.getLogger().println("Failed to get GeneXus executable");
                }
            }
            if (env != null) {
                gx = gx.forEnvironment(env);
            }
        }
        return gx;
    }

    @Initializer(after = EXTENSIONS_AUGMENTED)
    public static void onLoaded() {
        //Creates default tool installation if needed.
        GeneXusInstallation[] installations = GeneXusInstallation.getInstallations();
        if (installations != null && installations.length > 0) {
            LOGGER.log(Level.FINEST, "Already initialized GeneXusInstallation, no need to initialize again");
            //No need to initialize if there's already something
            return;
        }

        DescriptorImpl descriptor = ToolInstallation.all().get(DescriptorImpl.class);
        if (descriptor == null) {
            LOGGER.log(Level.INFO, "Could not find DescriptorImpl class for GeneXus Installation");
            return;
        }

        String defaultGxExecutable = GeneXusExecutable.GENEXUS.getName(!isWindows());
        GeneXusInstallation installation = new GeneXusInstallation(DEFAULT, defaultGxExecutable, "");
        descriptor.setInstallations(installation);
        descriptor.save();
    }

    private static final Logger LOGGER = Logger.getLogger(GeneXusInstallation.class.getName());

    /**
     * inline ${@link hudson.Functions#isWindows()} to prevent a transient
     * remote classloader issue
     */
    private static boolean isWindows() {
        return File.pathSeparatorChar == ';';
    }

    @Extension
    @Symbol("genexus")
    public static final class DescriptorImpl extends ToolDescriptor<GeneXusInstallation> {

        public DescriptorImpl() {
            super();
            load();
        }

        @Override
        public String getDisplayName() {
            return "GeneXus";
        }

        @Override
        public GeneXusInstallation[] getInstallations() {
            load();
            return super.getInstallations();
        }

        @Override
        public void setInstallations(GeneXusInstallation... installations) {
            super.setInstallations(installations);
            save();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            super.configure(req, json);
            save();
            return true;
        }

        /**
         *
         * @return MsBuildInstallation descriptor
         */
        public MsBuildInstallation.DescriptorImpl getMSBuildToolDescriptor() {
            return ToolInstallation.all().get(MsBuildInstallation.DescriptorImpl.class);
        }

        @RequirePOST
        public ListBoxModel doFillMsBuildInstallationIdItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("(Default)", "");

            Descriptor msbuildDescriptor = getMSBuildToolDescriptor();
            if (msbuildDescriptor != null) {
                for (MsBuildInstallation installation : getMSBuildToolDescriptor().getInstallations()) {
                    items.add(installation.getName(), installation.getName());
                }
            }

            return items;
        }
    }

    private static class FileOnNodeValidator extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final String basePath;
        private final String fileName;

        public FileOnNodeValidator(String basePath, String fileName) {
            this.basePath = basePath;
            this.fileName = fileName;
        }

        @Override
        public String call() throws IOException {
            File exe = new File(basePath, fileName);
            if (exe.exists()) {
                return exe.getPath();
            }
            return null;
        }
    }
}
