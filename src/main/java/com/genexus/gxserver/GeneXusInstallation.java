/*
 * The MIT License
 *
 * Copyright 2018 jlr.
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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    @DataBoundConstructor
    public GeneXusInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, null);
    }

    @Override
    public GeneXusInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new GeneXusInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    @Override
    public GeneXusInstallation forEnvironment(EnvVars environment) {
        return new GeneXusInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public String getExecutable(final GeneXusExecutable executable, Launcher launcher) throws IOException, InterruptedException { 
        return launcher.getChannel().call(new MasterToSlaveCallable<String, IOException>() { 
            @Override
            public String call() throws IOException { 
                File exe = new File(getHome(), executable.getName()); 
                if (exe.exists()) { 
                    return exe.getPath(); 
                } 
                return null; 
            } 
        }); 
    }
    
    public static GeneXusInstallation[] allInstallations() { 
        DescriptorImpl gxDescriptor = Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class); 
        return gxDescriptor.getInstallations(); 
    } 
 
    public static GeneXusInstallation getInstallation(String gxInstallation) throws IOException { 
        GeneXusInstallation[] installations = allInstallations(); 
        if (gxInstallation == null) { 
            if (installations.length == 0) { 
                throw new IOException("GeneXus Installation not found"); 
            } 
            return installations[0]; 
        } else { 
            for (GeneXusInstallation installation: installations) { 
                if (gxInstallation.equals(installation.getName())) { 
                    return installation; 
                } 
            } 
        } 
        throw new IOException("GeneXus Installation not found"); 
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
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException { 
            super.configure(req, json); 
            save(); 
            return true; 
        }
        
        @SuppressWarnings("unchecked")
        public List<ToolDescriptor<? extends GeneXusInstallation>> getApplicableDescriptors() {
            List<ToolDescriptor<? extends GeneXusInstallation>> r = new ArrayList<>();
            for (ToolDescriptor<?> td : Jenkins.getInstance().<ToolInstallation,ToolDescriptor<?>>getDescriptorList(ToolInstallation.class)) {
                if (GeneXusInstallation.class.isAssignableFrom(td.clazz)) { // This checks cast is allowed
                    r.add((ToolDescriptor<? extends GeneXusInstallation>)td); // This is the unchecked cast
                }
            }
            return r;
        }
    }
}
