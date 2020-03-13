package org.jenkinsci.plugins.genexus.helpers;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.CheckForNull;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Computer;
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

public class GxMsBuildHelper {

    private static String getMsBuildFullPath(Launcher launcher, String pathToTool, String execName)
            throws IOException, InterruptedException {
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

    private static String[] tokenizeArgs(String args) {
        if (args == null) {
            return null;
        }
        final String[] tokenize = Util.tokenize(args);
        if (args.endsWith("\\")) {
            tokenize[tokenize.length - 1] = tokenize[tokenize.length - 1] + "\\";
        }
        return tokenize;
    }

    @CheckForNull
    private static Node workspaceToNode(FilePath workspace, TaskListener listener) {
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

    public static void doCheckoutOrUpdate(Run<?, ?> build, MsBuildBuilder builder, BuildListener listener,
            Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        workspace.mkdirs();
        ArgumentListBuilder args = new ArgumentListBuilder();
        String execName = "msbuild.exe";
        MsBuildInstallation ai = builder.getMsBuild();
        // MsBuild path setup
        if (ai == null) {
            // if installation is null, default to search in workspace or node PATH
            listener.getLogger().println("Path To MSBuild.exe: " + execName);
            args.add(execName);
        } else {
            EnvVars env = build.getEnvironment(listener);
            Node node = workspaceToNode(workspace, listener);
            node.createPath(workspace.getRemote());
            if (node != null) {
                ai = ai.forNode(node, listener);
                ai = ai.forEnvironment(env);
                String pathToMsBuild = GxMsBuildHelper.getMsBuildFullPath(launcher, ai.getHome(), execName);
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
                    args.add(GxMsBuildHelper.tokenizeArgs(ai.getDefaultArgs()));
                }
            }
        }

        // Parameter setup
        EnvVars env = build.getEnvironment(listener);
        String normalizedArgs = builder.getCmdLineArgs().replaceAll("[\t\r\n]+", " ");
        normalizedArgs = Util.replaceMacro(normalizedArgs, env);
        if (normalizedArgs.trim().length() > 0)
            args.add(GxMsBuildHelper.tokenizeArgs(normalizedArgs));

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
            args.add("/nologo");
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
            listener.getLogger().println(r);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            listener.fatalError(e.getMessage());
            build.setResult(Result.FAILURE);
        }
    }

}