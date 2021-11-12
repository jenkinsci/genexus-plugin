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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
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
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author jlr
 */
public final class GeneXusInstallation extends ToolInstallation
        implements NodeSpecific<GeneXusInstallation>, EnvironmentSpecific<GeneXusInstallation> {

    private static final long serialVersionUID = 1L;

    private final String msBuildInstallationId;

    public GeneXusInstallation(String name, String home, String msBuildInstallationId) {
        this(name, home, msBuildInstallationId, Collections.<ToolProperty<?>>emptyList());
    }

    /**
     *
     * @param msBuildInstallationId MSBuild installation to use
     * @param name Installation name
     * @param home Path to GeneXus Installation
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

    public static GeneXusInstallation getInstallation(String installationId) {
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

    @Extension
    @Symbol("genexus")
    public static class DescriptorImpl extends ToolDescriptor<GeneXusInstallation> {

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
