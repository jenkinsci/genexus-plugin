/*
 * The MIT License
 *
 * Copyright 2020 GeneXus S.A..
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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Messages;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jlr
 *
 * Code and ideas taken from hudson.tasks.CommandInterpreter
 */
public class CommandBuilder {

    private final ArgumentListBuilder args;

    public CommandBuilder(ArgumentListBuilder args) {
        this.args = args;
    }

    public boolean perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException {
        int r = -1;

        try {
            EnvVars envVars = build.getEnvironment(listener);
            LOGGER.log(Level.FINE, "Executing command {0}", args.toString());
            r = join(launcher.launch().cmds(args).envs(envVars).stdout(listener).pwd(workspace).start());
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            Functions.printStackTrace(e, listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
        }
        return r == 0;
    }

    /**
     * Taken from CommandInterpreter.java
     *
     * Reports the exit code from the process.
     *
     */
    protected int join(Proc p) throws IOException, InterruptedException {
        return p.join();
    }

    private static final Logger LOGGER = Logger.getLogger(CommandBuilder.class.getName());
}
