/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Nikita Levyankov
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
package org.hudsonci.api.model;

import hudson.PermalinkList;
import hudson.model.BallColor;
import hudson.model.BuildTimelineWidget;
import hudson.model.Fingerprint;
import hudson.model.HealthReport;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.LogRotator;
import hudson.util.Graph;
import hudson.util.RunList;
import hudson.widgets.Widget;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Interface that represents Job.
 * <p/>
 * Date: 9/15/11
 *
 * @author Nikita Levyankov
 */
public interface IJob<JobT extends Job> extends Item {

    /**
     * @return whether the name of this job can be changed by user.
     */
    boolean isNameEditable();

    /**
     * Returns the log rotator for this job, or null if none.
     *
     * @return {@link LogRotator} instance.
     */
    LogRotator getLogRotator();

    /**
     * Sets log rotator.
     *
     * @param logRotator log rotator.
     */
    void setLogRotator(LogRotator logRotator);

    /**
     * @return true if this instance supports log rotation configuration.
     */
    boolean supportsLogRotator();

    /**
     * Gets all the job properties configured for this job.
     *
     * @return Map of properties.
     */
    Map<JobPropertyDescriptor, JobProperty<? super JobT>> getProperties();

    /**
     * @return List of all {@link JobProperty}.
     */
    List<JobProperty<? super JobT>> getAllProperties();

    /**
     * @return true if the build is in the queue.
     */
    boolean isInQueue();

    /**
     * @return queue item If this job is in the build queue.
     */
    Queue.Item getQueueItem();

    /**
     * @return true if a build of this project is in progress.
     */
    boolean isBuilding();

    /**
     * @return flag, which shows whether all the build logs of dependency components will be kept.
     */
    boolean isKeepDependencies();

    /**
     * Allocates a new buildCommand number.
     *
     * @return new build number.
     * @throws IOException if any.
     */
    int assignBuildNumber() throws IOException;

    /**
     * @return the next build number.
     */
    int getNextBuildNumber();

    /**
     * Programatically updates the next build number.
     * <p/>
     * <p/>
     * Much of Hudson assumes that the build number is unique and monotonic, so
     * this method can only accept a new value that's bigger than
     * {@link #getLastBuild()} returns. Otherwise it'll be no-op.
     *
     * @param next build number to set.
     * @throws IOException if any.
     * @since 1.199 (before that, this method was package private.)
     */
    void updateNextBuildNumber(int next) throws IOException;

    /**
     * Perform log rotation.
     *
     * @throws IOException          if any.
     * @throws InterruptedException if any.
     */
    void logRotate() throws IOException, InterruptedException;

    /**
     * @return {@link hudson.widgets.HistoryWidget}
     */
    List<Widget> getWidgets();

    /**
     * @return true if we should display "build now" icon
     */
    boolean isBuildable();

    /**
     * Gets all the {@link hudson.model.PermalinkProjectAction.Permalink}s defined for this job.
     *
     * @return never null
     */
    PermalinkList getPermalinks();

    /**
     * Gets the read-only view of all the builds.
     *
     * @return never null. The first entry is the latest build.
     */
    <RunT extends Run> RunList<RunT> getBuilds();

    /**
     * @param rs target rangeSet.
     * @return all the {@link Run}s whose build numbers matches the given {@link Fingerprint.RangeSet}.
     */
    <RunT extends Run> List<RunT> getBuilds(Fingerprint.RangeSet rs);

    /**
     * @return all the builds in a map.
     */
    <RunT extends Run> SortedMap<Integer, RunT> getBuildsAsMap();

    /**
     * @param n The build number.
     * @return null if no such build exists.
     * @see Run#getNumber()
     */
    <RunT extends Run> RunT getBuildByNumber(int n);

    /**
     * Gets the youngest build #m that satisfies <tt>n&lt;=m</tt>.
     * <p/>
     * This is useful when you'd like to fetch a build but the exact build might
     * be already gone (deleted, rotated, etc.)
     *
     * @param n build to compare with.
     * @return youngest build.
     */
    <RunT extends Run> RunT getNearestBuild(int n);

    /**
     * Gets the latest build #m that satisfies <tt>m&lt;=n</tt>.
     * <p/>
     * This is useful when you'd like to fetch a build but the exact build might
     * be already gone (deleted, rotated, etc.)
     *
     * @param n build to compare with.
     * @return the oldest build.
     */
    <RunT extends Run> RunT getNearestOldBuild(int n);

    /**
     * @return the last build
     */
    <RunT extends Run> RunT getLastBuild();

    /**
     * @return the oldest build in the record.
     */
    <RunT extends Run> RunT getFirstBuild();

    /**
     * @return the last successful build, if any. Otherwise null. A successful build
     *         would include either {@link Result#SUCCESS} or {@link Result#UNSTABLE}.
     * @see #getLastStableBuild()
     */
    <RunT extends Run> RunT getLastSuccessfulBuild();

    /**
     * @return the last build that was anything but stable, if any. Otherwise null.
     * @see #getLastSuccessfulBuild
     */
    <RunT extends Run> RunT getLastUnsuccessfulBuild();

    /**
     * @return the last unstable build, if any. Otherwise null.
     * @see #getLastSuccessfulBuild
     */
    <RunT extends Run> RunT getLastUnstableBuild();

    /**
     * @return the last stable build, if any. Otherwise null.
     * @see #getLastSuccessfulBuild
     */
    <RunT extends Run> RunT getLastStableBuild();

    /**
     * @return the last failed build, if any. Otherwise null.
     */
    <RunT extends Run> RunT getLastFailedBuild();

    /**
     * @return the last completed build, if any. Otherwise null.
     */
    <RunT extends Run> RunT getLastCompletedBuild();

    /**
     * Returns the last 'numberOfBuilds' builds with a build result >= 'threshold'
     *
     * @param numberOfBuilds build count to return.
     * @param threshold required {@link Result} of the build.
     * @return a list with the builds. May be smaller than 'numberOfBuilds' or even empty
     *         if not enough builds satisfying the threshold have been found. Never null.
     */
    <RunT extends Run> List<RunT> getLastBuildsOverThreshold(int numberOfBuilds, Result threshold);

    /**
     * @return build status image link. Info is taken from {@link BallColor#getImage()}.
     * @see #getIconColor()
     * @see BallColor#getImage()
     */
    String getBuildStatusUrl();

    /**
     * @return the color of the status ball for the project.
     */
    BallColor getIconColor();

    /**
     * Get the current health report for a job.
     *
     * @return the health report. Never returns null
     */
    HealthReport getBuildHealth();

    /**
     * @return list of {@link HealthReport}
     */
    List<HealthReport> getBuildHealthReports();

    /**
     * @return {@link Graph} of builds
     */
    Graph getBuildTimeGraph();

    /**
     * @return {@link BuildTimelineWidget} based on build history
     */
    BuildTimelineWidget getTimeline();

    /**
     * Returns the author of the job.
     *
     * @return the author of the job.
     * @since 2.0.1
     */
    String getCreatedBy();

    /**
     * Returns time when the project was created.
     *
     * @return time when the project was created.
     * @since 2.0.1
     */
    long getCreationTime();
}
