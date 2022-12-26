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
package org.jenkinsci.plugins.genexus.helpers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import static hudson.init.InitMilestone.EXTENSIONS_AUGMENTED;
import hudson.init.Initializer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.msbuild.MsBuildInstallation;
import hudson.tools.ToolInstallation;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jlr
 */
public class MsBuildInstallationHelper {

    private static MsBuildInstallation[] getInstallations() {
        MsBuildInstallation.DescriptorImpl descriptor = ToolInstallation.all().get(MsBuildInstallation.DescriptorImpl.class);
        if (descriptor == null) {
            return new MsBuildInstallation[]{};
        }

        return descriptor.getInstallations();
    }

    private static MsBuildInstallation getInstallation(String installationId) {
        if (installationId == null) {
            return null;
        }

        for (MsBuildInstallation i : getInstallations()) {
            if (installationId.equals(i.getName())) {
                return i;
            }
        }

        return null;
    }

    /**
     * Resolves MSBuild installation by name.
     *
     * @param installationId installation Id. If {@code null}, default
     * installation will be used (if exists)
     * @param builtOn Node for which the installation should be resolved Can be
     * {@link Jenkins#getInstance()} when running on controller
     * @param env Additional environment variables
     * @param listener Event listener
     * @return MSBuild installation or {@code null} if it cannot be resolved
     */
    @CheckForNull
    public static MsBuildInstallation resolveInstallation(@CheckForNull String installationId,
            @CheckForNull Node builtOn,
            @CheckForNull EnvVars env,
            @NonNull TaskListener listener) {

        MsBuildInstallation msb = null;
        if (StringUtils.isNotBlank(installationId)) {
            msb = getInstallation(installationId);
        }

        if (msb == null) {
            listener.getLogger().println("Selected GeneXus installation does not exist. Using Default");
            msb = getDefaultInstallation();
        }

        if (msb != null) {
            if (builtOn != null) {
                try {
                    msb = msb.forNode(builtOn, listener);
                } catch (IOException | InterruptedException e) {
                    listener.getLogger().println("Failed to get GeneXus executable");
                }
            }
            if (env != null) {
                msb = msb.forEnvironment(env);
            }
        }
        return msb;
    }

    @Initializer(after = EXTENSIONS_AUGMENTED)
    public static void onLoaded() {
        //Creates default tool installation if needed.
        MsBuildInstallation[] installations = getInstallations();
        if (installations != null && installations.length > 0) {
            LOGGER.log(Level.FINEST, "Already initialized MsBuildInstallation, no need to initialize again");
            //No need to initialize if there's already something
            return;
        }

        MsBuildInstallation.DescriptorImpl descriptor = ToolInstallation.all().get(MsBuildInstallation.DescriptorImpl.class);
        if (descriptor == null) {
            LOGGER.log(Level.INFO, "Could not find DescriptorImpl class for MSBuild Installation");
            return;
        }

        String defaultGxExecutable = getDefaultName(!isWindows());
        MsBuildInstallation installation = new MsBuildInstallation(DEFAULT, defaultGxExecutable, "");
        descriptor.setInstallations(installation);
        descriptor.save();
    }

    private static final Logger LOGGER = Logger.getLogger(MsBuildInstallation.class.getName());

    /**
     * Constant <code>DEFAULT="Default"</code>
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "not needed on deserialization")
    private static transient final String DEFAULT = "Default";

    /**
     * inline ${@link hudson.Functions#isWindows()} to prevent a transient
     * remote classloader issue
     */
    private static boolean isWindows() {
        return File.pathSeparatorChar == ';';
    }

    private static String getDefaultName(boolean isUnix) {
        final String defaultBaseName = "msbuild";
        if (isUnix) {
            return defaultBaseName;
        }

        return defaultBaseName + ".exe";
    }

    /**
     * Returns the default installation.
     *
     * @return default installation
     */
    private static MsBuildInstallation getDefaultInstallation() {
        MsBuildInstallation tool = getInstallation(DEFAULT);
        if (tool != null) {
            return tool;
        }

        MsBuildInstallation[] installations = getInstallations();
        if (installations.length == 0) {
            onLoaded();
            installations = getInstallations();
        }

        if (installations.length > 0) {
            return installations[0];
        }

        return null;
    }
}
