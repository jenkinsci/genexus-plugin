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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import org.apache.commons.io.IOCase;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.msbuild.MSBuildConsoleAnnotator;
import hudson.plugins.msbuild.MsBuildBuilder;
import hudson.plugins.msbuild.MsBuildConsoleParser;
import hudson.plugins.msbuild.MsBuildInstallation;
import hudson.util.ArgumentListBuilder;
import jenkins.model.Jenkins;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates the msbuild call for checkout/update
 */

public class GXSMsBuildExe {

    private final Run<?, ?> build;
    private final Launcher launcher;
    private final FilePath workspace;
    private final TaskListener listener;
    private final MsBuildBuilder builder;
    private final FilePath workingDirectory;
    private final String execName = "msbuild.exe";
    private final GXSMsBuildExeArgs buildargs;

    // msbuild arguments

    public GXSMsBuildExe(Run<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener, Item context,
            FilePath workingDirectory, MsBuildBuilder builder, GXSMsBuildExeArgs buildargs) {
        this.build = build;
        this.launcher = launcher;
        this.workspace = workspace;
        this.listener = listener;
        this.workingDirectory = workingDirectory;
        this.builder = builder;
        this.buildargs = buildargs;
    }

    private boolean kbAlreadyExists() {
        WildcardFileFilter filter = new WildcardFileFilter("*.gxw", IOCase.INSENSITIVE);
        try {
            return !(workingDirectory.list(filter).isEmpty());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(GeneXusServerSCM.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private ArgumentListBuilder getArguments(ArgumentListBuilder args) {
        args.add("/nologo");
        args.add("/p:GX_PROGRAM_DIR=\"" + buildargs.getGxpath() + "\"");
        args.add("/p:WorkingDirectory=\"" + workingDirectory + "\"");
        if (buildargs.getServerCredentials() != null) {
            args.addMasked("/p:ServerUsername=\"" + buildargs.getServerCredentials().getUsername() + "\"");
            args.addMasked("/p:ServerPassword=\"" + buildargs.getServerCredentials().getPassword().getPlainText() + "\"");
        }
        if (StringUtils.isNotBlank(buildargs.getKbVersion())) {
            args.add("/p:ServerKbVersion:\"" + buildargs.getKbVersion() + "\"");
        }
        if (buildargs.getKbDbCredentials() != null) {
            args.add("/p:DbaseUseIntegratedSecurity=false");
            args.addMasked("/p:DbaseServerUsername=\"" + buildargs.getKbDbCredentials().getUsername() + "\"");
            args.addMasked("/p:DbaseServerPassword=\"" + buildargs.getKbDbCredentials().getPassword().getPlainText() + "\"");
        }
        if (kbAlreadyExists()) {
            listener.getLogger().println("Knowledge base was found. Running update.");
            if (StringUtils.isNotBlank(buildargs.getKbVersion())) {
                args.add("/p:WorkingVersion=\"" + buildargs.getKbVersion() + "\"");
                args.add("/t:Update");
            }
        } else {
            listener.getLogger().println("Knowledge base was not found. Running checkout.");
            args.add("/p:ServerUrl=\"" + buildargs.getServerUrl() + "\"");
            args.add("/p:ServerKbAlias=\"" + buildargs.getKbName() + "\"");
            if (StringUtils.isNotBlank(buildargs.getKbDbServerInstance())) {
                args.add("/p:DbaseServerInstance=\"" + buildargs.getKbDbServerInstance() + "\"");
            }
            args.add("/p:DbaseName=\"" + buildargs.getKbDbName() + "\"");
            args.add("/p:CreateDbInKbFolder=" + buildargs.getKbDbInSameFolder());
            args.add("/t:Checkout");
        }
        return args;
    }

    public void launch() throws Exception {
        ArgumentListBuilder args = new ArgumentListBuilder();
        MsBuildInstallation ai = builder.getMsBuild();
        EnvVars env = build.getEnvironment(listener);
        // MsBuild path setup
        if (ai == null) {
            // if installation is null, default to search in workspace or node PATH
            listener.getLogger().println("Path To MSBuild.exe: " + execName);
            args.add(execName);
        } else {
            Node node = workspaceToNode();
            if (node != null) {
                ai = ai.forNode(node, listener);
                ai = ai.forEnvironment(env);
                String pathToMsBuild = getMsBuildFullPath(ai.getHome(), execName);
                FilePath exec = new FilePath(launcher.getChannel(), pathToMsBuild);
                try {
                    if (!exec.exists()) {
                        listener.fatalError(pathToMsBuild + " doesn't exist");
                    }
                } catch (IOException e) {
                    listener.fatalError("Failed checking for existence of " + pathToMsBuild);
                }
                listener.getLogger().println("Path To MSBuild.exe: " + pathToMsBuild);
                args.add(pathToMsBuild);
                if (ai.getDefaultArgs() != null) {
                    args.add(tokenizeArgs(ai.getDefaultArgs()));
                }
            }
        }

        args = getArguments(args);

        // Parameter setup
        //String normalizedArgs = builder.getCmdLineArgs().replaceAll("[\t\r\n]+", " ");
        //normalizedArgs = Util.replaceMacro(normalizedArgs, env);
        //if (normalizedArgs.trim().length() > 0)
        //    args.add(tokenizeArgs(normalizedArgs));

        String normalizedFile = null;
        String msBuildFile = builder.getMsBuildFile();
        if (msBuildFile != null && msBuildFile.trim().length() != 0) {
            normalizedFile = msBuildFile.replaceAll("[\t\r\n]+", " ");
            normalizedFile = Util.replaceMacro(normalizedFile, env);
            if (!normalizedFile.isEmpty()) {
                args.add(normalizedFile);
            }
        }

        if (!launcher.isUnix()) {
            args.prepend("cmd.exe", "/C", "\"");
            args.add("\"", "&&", "exit", "%ERRORLEVEL%");
        } else {
            listener.fatalError("Unable to use this plugin on this kind of operation system");
        }
        try {
            // Parser to find the number of Warnings/Errors
            MsBuildConsoleParser mbcp = new MsBuildConsoleParser(listener.getLogger(), build.getCharset());
            MSBuildConsoleAnnotator annotator = new MSBuildConsoleAnnotator(listener.getLogger(), build.getCharset());

            // Launch the msbuild.exe
            int r = launcher.launch().cmds(args).envs(env).stdout(mbcp).stdout(annotator).pwd(workspace).join();
            if(r != 0){
                listener.fatalError("There was an error on running the msbuild task.");
                build.setResult(Result.FAILURE);
                throw new Exception("There was an error on running the msbuild task.");
            }
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            listener.fatalError(e.getMessage());
            build.setResult(Result.FAILURE);
        }
    }

    /**
     * Obtains the node the workspace is from
     *
     * @return
     */
    @CheckForNull
    private Node workspaceToNode() {
        Jenkins j = Jenkins.get();
        if (j != null && workspace.isRemote()) {
            for (Computer c : j.getComputers()) {
                if (c.getChannel() == workspace.getChannel()) {
                    Node n = c.getNode();
                    if (n != null) {
                        return n;
                    }
                }
            }
        } else {
            listener.fatalError("Jenkins instance is not ready.");
        }
        return j;
    }

    private String[] tokenizeArgs(String args) {
        if (args == null) {
            return null;
        }
        final String[] tokenize = Util.tokenize(args);
        if (args.endsWith("\\")) {
            tokenize[tokenize.length - 1] = tokenize[tokenize.length - 1] + "\\";
        }
        return tokenize;
    }

    /**
     * Obtains the full path of the msbuild tool
     *
     * @param pathToTool
     * @param execName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String getMsBuildFullPath(String pathToTool, String execName) throws IOException, InterruptedException {
        String fullPathToMsBuild = (pathToTool != null ? pathToTool : "");
        FilePath exec = new FilePath(launcher.getChannel(), fullPathToMsBuild);
        if (exec.isDirectory()) {
            if (!fullPathToMsBuild.endsWith("\\")) {
                fullPathToMsBuild = fullPathToMsBuild + "\\";
            }
            fullPathToMsBuild = fullPathToMsBuild + execName;
        }
        return fullPathToMsBuild;
    }

}