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
package com.genexus.gxserver;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author jlr
 */
public class GeneXusBuilder extends Builder {

    /**
     * Identifies {@link GeneXusInstallation} to be used.
     */
    public final String gxName;

    @DataBoundConstructor
    public GeneXusBuilder(String gxName) {
        this.gxName = gxName;
    }

    /**
     * Gets the GeneXusInstallation to invoke,
     * or null to invoke the default one.
     * @return GeneXusInstallation to invoke.
     */
    public GeneXusInstallation getGeneXus() {
        for( GeneXusInstallation i : getDescriptor().getInstallations() ) {
            if(gxName !=null && gxName.equals(i.getName()))
                return i;
        }
        return null;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        EnvVars env = build.getEnvironment(listener);

        ArgumentListBuilder args = new ArgumentListBuilder();
        GeneXusInstallation mi = getGeneXus();
        if(mi==null) {
            String execName = "GeneXus.exe";
            args.add(execName);
        } else {
            Node node = Computer.currentComputer().getNode();
            if (node != null) {
                mi = mi.forNode(node, listener);
                mi = mi.forEnvironment(env);
                String exec = mi.getExecutable(launcher);
                if(exec==null) {
                    listener.fatalError("Could not find GeneXus executable at " + mi.getHome());
                    return false;
                }
                args.add(exec);
                mi.buildEnvVars(env);
            }
        }

        try {
            args = args.toWindowsCommand();
            int r = launcher.launch().cmds(args).envs(env).stdout(listener.getLogger()).pwd(build.getModuleRoot()).join();
            if (0 != r) {
                return false;
            }
        } catch (IOException e) {
            Util.displayIOException(e,listener);
            Functions.printStackTrace(e, listener.fatalError("Failed to execute GeneXus command"));
            return false;
        }
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension @Symbol("genexus")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @CopyOnWrite
        private volatile GeneXusInstallation[] installations = new GeneXusInstallation[0];

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getHelpFile(String fieldName) {
            if (fieldName != null && fieldName.equals("globalSettings")) fieldName = "settings"; // same help file
            return super.getHelpFile(fieldName);
        }

        @Override
        public String getDisplayName() {
            return "GeneXus";
        }
        
        public GeneXusInstallation[] getInstallations() {
            return Arrays.copyOf(installations, installations.length);
        }

        public void setInstallations(GeneXusInstallation... installations) {
            List<GeneXusInstallation> tmpList = new ArrayList<>();
            // remote empty Maven installation : 
            if(installations != null) {
                    Collections.addAll(tmpList, installations);
                    for(GeneXusInstallation installation : installations) {
                            if(Util.fixEmptyAndTrim(installation.getName()) == null) {
                                    tmpList.remove(installation);
                            }
                    }
            }
            this.installations = tmpList.toArray(new GeneXusInstallation[tmpList.size()]);
            save();
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (req == null) {
                // This state is prohibited according to the Javadoc of the super method.
                throw new FormException("GeneXus Build Step new instance method is called for null Stapler request. "
                        + "Such call is prohibited.", "req");
            }
            return req.bindJSON(GeneXusBuilder.class,formData);
        }
    }
}