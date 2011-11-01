/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Martin Eigenbrodt,
 * Anton Kozak, Nikita Levyankov
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
package hudson.tasks;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;

/**
 * Deletes old log files.
 *
 * TODO: is there any other task that follows the same pattern?
 * try to generalize this just like {@link SCM} or {@link BuildStep}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogRotator implements Describable<LogRotator> {

    private static final Logger LOGGER = Logger.getLogger(LogRotator.class.getName());

    /**
     * If not -1, history is only kept up to this days.
     */
    private final int daysToKeep;

    /**
     * If not -1, only this number of build logs are kept.
     */
    private final int numToKeep;

    /**
     * If not -1 nor null, artifacts are only kept up to this days.
     * Null handling is necessary to remain data compatible with old versions.
     * @since 1.350
     */
    private final Integer artifactDaysToKeep;

    /**
     * If not -1 nor null, only this number of builds have their artifacts kept.
     * Null handling is necessary to remain data compatible with old versions.
     * @since 1.350
     */
    private final Integer artifactNumToKeep;

    @DataBoundConstructor
    public LogRotator (String logrotate_days, String logrotate_nums, String logrotate_artifact_days, String logrotate_artifact_nums) {
        this (parse(logrotate_days),parse(logrotate_nums),
              parse(logrotate_artifact_days),parse(logrotate_artifact_nums));
    }

    public static int parse(String p) {
        if(p==null)     return -1;
        try {
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * @deprecated since 1.350.
     *      Use {@link #LogRotator(int, int, int, int)}
     */
    public LogRotator(int daysToKeep, int numToKeep) {
        this(daysToKeep, numToKeep, -1, -1);
    }

    public LogRotator(int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep) {
        this.daysToKeep = daysToKeep;
        this.numToKeep = numToKeep;
        this.artifactDaysToKeep = artifactDaysToKeep;
        this.artifactNumToKeep = artifactNumToKeep;

    }

    public void perform(Job<?, ?> job) throws IOException, InterruptedException {
        LOGGER.log(FINE, "Running the log rotation for " + job.getFullDisplayName());

        // keep the last successful build regardless of the status
        Run lsb = job.getLastSuccessfulBuild();
        Run lstb = job.getLastStableBuild();

        List<? extends Run<?, ?>> builds = job.getBuilds();
        Calendar cal = null;
        //Delete builds
        if (-1 != numToKeep || -1 != daysToKeep) {
            if (-1 != daysToKeep) {
                cal = new GregorianCalendar();
                cal.add(Calendar.DAY_OF_YEAR, -daysToKeep);
            }
            if (-1 != numToKeep) {
                builds = builds.subList(Math.min(builds.size(), numToKeep), builds.size());
            }
            //Delete builds based on configured values. See http://issues.hudson-ci.org/browse/HUDSON-3650
            deleteBuilds(builds, lsb, lstb, cal);
        }

        cal = null;
        builds = job.getBuilds();
        //Delete build artifacts
        if (-1 != artifactNumToKeep || -1 != artifactDaysToKeep) {
            if (-1 != artifactDaysToKeep) {
                cal = new GregorianCalendar();
                cal.add(Calendar.DAY_OF_YEAR, -artifactDaysToKeep);
            }
            if (-1 != artifactNumToKeep) {
                builds = builds.subList(Math.min(builds.size(), artifactNumToKeep), builds.size());
            }
            //Delete build artifacts based on configured values. See http://issues.hudson-ci.org/browse/HUDSON-3650
            deleteBuildArtifacts(builds, lsb, lstb, cal);
        }
    }

    /**
     * Performs builds deletion
     *
     * @param builds list of builds
     * @param lastSuccessBuild last success build
     * @param lastStableBuild last stable build
     * @param cal calendar if configured
     * @throws IOException if configured
     */
    private void deleteBuilds(List<? extends Run<?, ?>> builds, Run lastSuccessBuild, Run lastStableBuild, Calendar cal)
        throws IOException {
        for (Run currentBuild : builds) {
            if (allowDeleteBuild(lastSuccessBuild, lastStableBuild, currentBuild, cal)) {
                LOGGER.log(FINER, currentBuild.getFullDisplayName() + " is to be removed");
                currentBuild.delete();
            }
        }
    }

    /**
     * Checks whether current build could be deleted.
     * If current build equals to last Success Build or last Stable Build or currentBuild is configured to keep logs or
     * currentBuild timestamp is before configured calendar value - return false, otherwise return true.
     *
     * @param lastSuccessBuild {@link Run}
     * @param lastStableBuild {@link Run}
     * @param currentBuild {@link Run}
     * @param cal {@link Calendar}
     * @return true - if deletion is allowed, false - otherwise.
     */
    private boolean allowDeleteBuild(Run lastSuccessBuild, Run lastStableBuild, Run currentBuild, Calendar cal) {
        if (currentBuild.isKeepLog()) {
            LOGGER.log(FINER, currentBuild.getFullDisplayName() + " is not GC-ed because it's marked as a keeper");
            return false;
        }
        if (currentBuild == lastSuccessBuild) {
            LOGGER.log(FINER,
                currentBuild.getFullDisplayName() + " is not GC-ed because it's the last successful build");
            return false;
        }
        if (currentBuild == lastStableBuild) {
            LOGGER.log(FINER, currentBuild.getFullDisplayName() + " is not GC-ed because it's the last stable build");
            return false;
        }
        if (null != cal && !currentBuild.getTimestamp().before(cal)) {
            LOGGER.log(FINER, currentBuild.getFullDisplayName() + " is not GC-ed because it's still new");
            return false;
        }
        return true;
    }

    /**
     * Performs build artifacts deletion
     *
     * @param builds list of builds
     * @param lastSuccessBuild last success build
     * @param lastStableBuild last stable build
     * @param cal calendar if configured
     * @throws IOException if configured
     */
    private void deleteBuildArtifacts(List<? extends Run<?, ?>> builds, Run lastSuccessBuild, Run lastStableBuild,
                                      Calendar cal) throws IOException {
        for (Run currentBuild : builds) {
            if (allowDeleteArtifact(lastSuccessBuild, lastStableBuild, currentBuild, cal)) {
                currentBuild.deleteArtifacts();
            }
        }
    }

    /**
     * Checks whether artifacts from build could be deleted.
     * If current build equals to last Success Build or last Stable Build or currentBuild is configured to keep logs or
     * currentBuild timestamp is before configured calendar value - return false, otherwise return true.
     *
     * @param lastSuccessBuild {@link Run}
     * @param lastStableBuild {@link Run}
     * @param currentBuild {@link Run}
     * @param cal {@link Calendar}
     * @return true - if deletion is allowed, false - otherwise.
     */
    private boolean allowDeleteArtifact(Run lastSuccessBuild, Run lastStableBuild, Run currentBuild, Calendar cal) {
        if (currentBuild.isKeepLog()) {
            LOGGER.log(FINER,
                currentBuild.getFullDisplayName() + " is not purged of artifacts because it's marked as a keeper");
            return false;
        }
        if (currentBuild == lastSuccessBuild) {
            LOGGER.log(FINER, currentBuild.getFullDisplayName()
                + " is not purged of artifacts because it's the last successful build");
            return false;
        }
        if (currentBuild == lastStableBuild) {
            LOGGER.log(FINER,
                currentBuild.getFullDisplayName() + " is not purged of artifacts because it's the last stable build");
            return false;
        }
        if (null != cal && !currentBuild.getTimestamp().before(cal)) {
            LOGGER.log(FINER, currentBuild.getFullDisplayName() + " is not purged of artifacts because it's still new");
            return false;
        }
        return true;
    }

    public int getDaysToKeep() {
        return daysToKeep;
    }

    public int getNumToKeep() {
        return numToKeep;
    }

    public int getArtifactDaysToKeep() {
        return unbox(artifactDaysToKeep);
    }

    public int getArtifactNumToKeep() {
        return unbox(artifactNumToKeep);
    }

    public String getDaysToKeepStr() {
        return toString(daysToKeep);
    }

    public String getNumToKeepStr() {
        return toString(numToKeep);
    }

    public String getArtifactDaysToKeepStr() {
        return toString(artifactDaysToKeep);
    }

    public String getArtifactNumToKeepStr() {
        return toString(artifactNumToKeep);
    }

    private int unbox(Integer i) {
        return i==null ? -1: i;
    }

    private String toString(Integer i) {
        if (i==null || i==-1)   return "";
        return String.valueOf(i);
    }


    public LRDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public static final LRDescriptor DESCRIPTOR = new LRDescriptor();

    public static final class LRDescriptor extends Descriptor<LogRotator> {
        public String getDisplayName() {
            return "Log Rotation";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LogRotator that = (LogRotator) o;

        if (daysToKeep != that.daysToKeep) {
            return false;
        }
        if (numToKeep != that.numToKeep) {
            return false;
        }
        if (artifactDaysToKeep != null ? !artifactDaysToKeep.equals(that.artifactDaysToKeep)
            : that.artifactDaysToKeep != null) {
            return false;
        }
        if (artifactNumToKeep != null ? !artifactNumToKeep.equals(that.artifactNumToKeep)
            : that.artifactNumToKeep != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = daysToKeep;
        result = 31 * result + numToKeep;
        result = 31 * result + (artifactDaysToKeep != null ? artifactDaysToKeep.hashCode() : 0);
        result = 31 * result + (artifactNumToKeep != null ? artifactNumToKeep.hashCode() : 0);
        return result;
    }
}
