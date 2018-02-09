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
package org.jenkinsci.plugins.genexus.builders;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.plugins.msbuild.MsBuildBuilder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import java.io.IOException;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.genexus.GeneXusInstallation;
import org.jenkinsci.plugins.genexus.helpers.MsBuildArgsHelper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

//TODO: 
// - convertir esta clase en abstracta y sacarle el perform
// - hacer una derivada que efectivamente ejecute gx
// - hacer otra que ejecute el build

/**
 *
 * @author jlr
 */
public class GeneXusBuilder extends Builder {

    /**
     * Identifies {@link GeneXusInstallation} to be used.
     */
    private final String gxInstallationId;
    private final String kbPath;
    private final String kbVersion;
    private final String kbEnvironment;
    private final boolean forceRebuild;


    @DataBoundConstructor
    public GeneXusBuilder(String gxInstallationId, String kbPath, String kbVersion, String kbEnvironment, boolean forceRebuild) {
        this.gxInstallationId = gxInstallationId;
        this.kbPath = kbPath;
        this.kbVersion = kbVersion;
        this.kbEnvironment = kbEnvironment;
        this.forceRebuild = forceRebuild;
    }

    @Exported
    public String getGxInstallationId() {
        return gxInstallationId;
    }
    
    @Exported
    public String getKbPath() {
        return kbPath;
    }
    
    @Exported
    public String getKbVersion() {
        return kbVersion;
    }
    
    @Exported
    public String getKbEnvironment() {
        return kbEnvironment;
    }
    
    @Exported
    public boolean getForceRebuild() {
        return forceRebuild;
    }
    
    private GeneXusInstallation getGeneXusInstallation() {
        return GeneXusInstallation.getInstallation(gxInstallationId);
    }
    
    private String getMSBuildInstallationId() {
        GeneXusInstallation installation = getGeneXusInstallation();
        if (installation!=null) {
            return installation.getMsBuildInstallationId();
        }
        
        return "";
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        GeneXusInstallation installation = getGeneXusInstallation();
        if(installation==null) {
            listener.fatalError("Could not find GeneXus Installation: " + gxInstallationId);
            return false;
        }

        String msBuildFile = installation.getFilePath("TeamDev.msbuild", launcher);
        if (msBuildFile == null) {
            listener.fatalError("Could not find TeamDev.msbuild from GeneXus Installation " + installation.getName());
            return false;
        }
        
        MsBuildArgsHelper argsHelper = new MsBuildArgsHelper("Build");
        argsHelper.addNoLogo();
        
        String gxHome = installation.getHome();
        if (gxHome != null)
            argsHelper.addProperty("GX_PROGRAM_DIR", gxHome);
        
        argsHelper.addProperty("WorkingDirectory", kbPath);
        argsHelper.addProperty("WorkingVersion", kbVersion);
        argsHelper.addProperty("WorkingEnvironment", kbEnvironment);
        argsHelper.addProperty("ForceRebuild", forceRebuild);
        
        MsBuildBuilder builder = new MsBuildBuilder(
                installation.getMsBuildInstallationId(),
                msBuildFile,
                argsHelper.toString(),
                true,
                false,
                true,
                false
        );

        return builder.perform(build, launcher, listener);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension @Symbol("genexusb")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public ListBoxModel doFillGxInstallationIdItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("(Default)", "");
            for (GeneXusInstallation installation : GeneXusInstallation.getInstallations()) {
                items.add(installation.getName(), installation.getName());
            }
            return items;
        }
        
        @Override
        public String getDisplayName() {
            return "Build GeneXus KB";
        }
    }
}