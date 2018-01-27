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
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.msbuild.MsBuildInstallation;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import jenkins.model.Jenkins;
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
        implements NodeSpecific<GeneXusInstallation>, EnvironmentSpecific<GeneXusInstallation>, Serializable {

    private final String msBuildInstallationId;

    /**
     *
     * @param msBuildInstallationId MSBuild installation to use
     * @param name Installation name
     * @param home Path to GeneXus Installation
     */
    @DataBoundConstructor
    public GeneXusInstallation(String name, String home, String msBuildInstallationId) {
        super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), null);
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
        return new GeneXusInstallation(getName(), environment.expand(getHome()),getMsBuildInstallationId());
    }

    public String getExecutable(final GeneXusExecutable executable, Launcher launcher) throws IOException, InterruptedException { 
        return launcher.getChannel().call(new MasterToSlaveCallable<String, IOException>() { 
            @Override
            public String call() throws IOException { 
                String gxHome = Util.replaceMacro(getHome(),EnvVars.masterEnvVars);
                File exe = new File(gxHome, executable.getName(launcher.isUnix())); 
                if (exe.exists()) { 
                    return exe.getPath(); 
                }
                return null; 
            } 
        }); 
    }
    
    String getExecutable(Launcher launcher) throws IOException, InterruptedException {
        return getExecutable(GeneXusExecutable.GENEXUS, launcher);
    }    

    public static GeneXusInstallation getInstallation(String installationId) {
        if (installationId == null)
            return null;

        DescriptorImpl descriptor = ToolInstallation.all().get(DescriptorImpl.class);
        if (descriptor == null)
            return null;
        
        for( GeneXusInstallation i : descriptor.getInstallations() ) {
            if(installationId.equals(i.getName()))
                return i;
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
            return Jenkins.getInstance().getDescriptorByType(GeneXusBuilder.DescriptorImpl.class).getInstallations();
        }

        @Override
        public void setInstallations(GeneXusInstallation... installations) {
            Jenkins.getInstance().getDescriptorByType(GeneXusBuilder.DescriptorImpl.class).setInstallations(installations);
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
    }
}
