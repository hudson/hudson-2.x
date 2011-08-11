/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Inc., Kohsuke Kawaguchi, Brian Westrich,   Jean-Baptiste Quenot, Anton Kozak
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Copies the artifacts into an archive directory.
 *
 * @author Kohsuke Kawaguchi
 */
public class ArtifactArchiver extends Recorder {

    /**
     * Comma- or space-separated list of patterns of files/directories to be archived.
     */
    private final String artifacts;

    /**
     * Possibly null 'excludes' pattern as in Ant.
     */
    private final String excludes;

    /**
     * Type of compression with will be applied to artifacts before master<->slave transfer.
     */
    private FilePath.TarCompression compressionType;

    /**
     * Just keep the last successful artifact set, no more.
     */
    private final boolean latestOnly;

    private final boolean autoValidateFileMask;
    
    private static final Boolean allowEmptyArchive = 
    	Boolean.getBoolean(ArtifactArchiver.class.getName()+".warnOnEmpty");

    /**
     * @deprecated as of 2.0.1
     */
    @Deprecated
    public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly) {
        this(artifacts, excludes, latestOnly, FilePath.TarCompression.GZIP.name());
    }
    /**
     * @deprecated as of 2.0.2
     */
    @Deprecated
    public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly, String compressionType) {
        this(artifacts, excludes, latestOnly, compressionType, false);
    }

    @DataBoundConstructor
    public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly, String compressionType,
                            boolean autoValidateFileMask) {
        this.artifacts = Util.fixEmptyAndTrim(artifacts);
        this.excludes = Util.fixEmptyAndTrim(excludes);
        this.latestOnly = latestOnly;
        setCompressionType(compressionType);
        this.autoValidateFileMask = autoValidateFileMask;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean needsToRun(Result buildResult) {
        //TODO it seems we shouldn't archive if build result is worse than SUCCESS, investigate this
        return buildResult.isBetterThan(Result.ABORTED);
    }

    public String getArtifacts() {
        return artifacts;
    }

    public String getExcludes() {
        return excludes;
    }

    public boolean isLatestOnly() {
        return latestOnly;
    }

    public boolean isAutoValidateFileMask() {
        return autoValidateFileMask;
    }

    /**
     * Returns compression type.
     *
     * @return compression type.
     */
    public FilePath.TarCompression getCompressionType() {
        return compressionType;
    }

    /**
     * Sets compression type.
     *
     * @param compression compression type.
     */
    public void setCompressionType(String compression) {
        try {
            compressionType = (compression != null ? FilePath.TarCompression.valueOf( compression) :
                FilePath.TarCompression.GZIP);
        } catch (IllegalArgumentException e) {
            compressionType = FilePath.TarCompression.GZIP;
        }
    }

    private void listenerWarnOrError(BuildListener listener, String message) {
        if (allowEmptyArchive) {
            listener.getLogger().println(String.format("WARN: %s", message));
        } else {
            listener.error(message);
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
        throws InterruptedException {
        if (artifacts.length() == 0) {
            listener.error(Messages.ArtifactArchiver_NoIncludes());
            build.setResult(Result.FAILURE);
            return true;
        }

        File dir = build.getArtifactsDir();
        dir.mkdirs();

        listener.getLogger().println(Messages.ArtifactArchiver_ARCHIVING_ARTIFACTS());
        try {
            FilePath ws = build.getWorkspace();
            if (ws == null) { // #3330: slave down?
                return true;
            }

            String artifacts = build.getEnvironment(listener).expand(this.artifacts);
            if (ws.copyRecursiveTo(artifacts, excludes, new FilePath(dir), compressionType) == 0) {
                if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
                    // If the build failed, don't complain that there was no matching artifact.
                    // The build probably didn't even get to the point where it produces artifacts.
                    listenerWarnOrError(listener, Messages.ArtifactArchiver_NoMatchFound(artifacts));
                    String msg = null;
                    try {
                        msg = ws.validateAntFileMask(artifacts);
                    } catch (Exception e) {
                        listenerWarnOrError(listener, e.getMessage());
                    }
                    if (msg != null) {
                        listenerWarnOrError(listener, msg);
                    }
                }
                if (!allowEmptyArchive) {
                    build.setResult(Result.FAILURE);
                }
                return true;
            }
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.error(Messages.ArtifactArchiver_FailedToArchive(artifacts)));
            build.setResult(Result.FAILURE);
            return true;
        }

        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if(latestOnly) {
            AbstractBuild<?,?> b = build.getProject().getLastCompletedBuild();
            Result bestResultSoFar = Result.NOT_BUILT;
            while(b!=null) {
                if (b.getResult().isBetterThan(bestResultSoFar)) {
                    bestResultSoFar = b.getResult();
                } else {
                    // remove old artifacts
                    File ad = b.getArtifactsDir();
                    if(ad.exists()) {
                        listener.getLogger().println(Messages.ArtifactArchiver_DeletingOld(b.getDisplayName()));
                        try {
                            Util.deleteRecursive(ad);
                        } catch (IOException e) {
                            e.printStackTrace(listener.error(e.getMessage()));
                        }
                    }
                }
                b = b.getPreviousBuild();
            }
        }
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    /**
     * @deprecated as of 1.286
     *      Some plugin depends on this, so this field is left here and points to the last created instance.
     *      Use {@link Hudson#getDescriptorByType(Class)} instead.
     */
    public static volatile DescriptorImpl DESCRIPTOR;

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            DESCRIPTOR = this; // backward compatibility
        }

        public String getDisplayName() {
            return Messages.ArtifactArchiver_DisplayName();
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public FormValidation doCheckArtifacts(@AncestorInPath AbstractProject project,
                                               @QueryParameter String artifacts,
                                               @QueryParameter boolean force) throws IOException {
            if (!force) {
                return FormValidation.ok();
            }
            return FilePath.validateFileMask(project.getSomeWorkspace(), artifacts);
        }

        @Override
        public ArtifactArchiver newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(ArtifactArchiver.class,formData);
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
