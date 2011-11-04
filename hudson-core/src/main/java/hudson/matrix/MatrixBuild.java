/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Red Hat, Inc., Tom Huybrechts, Anton Kozak
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
package hudson.matrix;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Executor;
import hudson.model.Fingerprint;
import hudson.model.Hudson;
import hudson.model.JobProperty;
import hudson.model.Node;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.slaves.WorkspaceList;
import hudson.slaves.WorkspaceList.Lease;
import hudson.tasks.Publisher;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Build of {@link MatrixProject}.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixBuild extends AbstractBuild<MatrixProject, MatrixBuild> {
    private AxisList axes;

    public MatrixBuild(MatrixProject job) throws IOException {
        super(job);
    }

    public MatrixBuild(MatrixProject job, Calendar timestamp) {
        super(job, timestamp);
    }

    public MatrixBuild(MatrixProject project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    public Object readResolve() {
        // MatrixBuild.axes added in 1.285; default to parent axes for old data
        if (axes == null) {
            axes = getParent().getAxes();
        }
        return this;
    }

    /**
     * Used by view to render a ball for {@link MatrixRun}.
     */
    public final class RunPtr {
        //TODO: review and check whether we can do it private
        public final Combination combination;

        private RunPtr(Combination c) {
            this.combination = c;
        }

        public MatrixRun getRun() {
            return MatrixBuild.this.getRun(combination);
        }

        public String getShortUrl() {
            return Util.rawEncode(combination.toString());
        }

        public String getTooltip() {
            MatrixRun r = getRun();
            if (r != null) {
                return r.getIconColor().getDescription();
            }
            Queue.Item item = Hudson.getInstance().getQueue().getItem(getParent().getItem(combination));
            if (item != null) {
                return item.getWhy();
            }
            return null;    // fall back
        }

        public Combination getCombination() {
            return combination;
        }
    }

    public Layouter<RunPtr> getLayouter() {
        // axes can be null if build page is access right when build starts
        return axes == null ? null : new Layouter<RunPtr>(axes) {
            protected RunPtr getT(Combination c) {
                return new RunPtr(c);
            }
        };
    }

    /**
     * Gets the {@link MatrixRun} in this build that corresponds
     * to the given combination.
     */
    public MatrixRun getRun(Combination c) {
        MatrixConfiguration config = getParent().getItem(c);
        if (config == null) {
            return null;
        }
        return config.getBuildByNumber(getNumber());
    }

    /**
     * Returns all {@link MatrixRun}s for this {@link MatrixBuild}.
     */
    public List<MatrixRun> getRuns() {
        List<MatrixRun> r = new ArrayList<MatrixRun>();
        for (MatrixConfiguration c : getParent().getItems()) {
            MatrixRun b = c.getBuildByNumber(getNumber());
            if (b != null) {
                r.add(b);
            }
        }
        return r;
    }

    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        try {
            MatrixRun item = getRun(Combination.fromString(token));
            if (item != null) {
                return item;
            }
        } catch (IllegalArgumentException _) {
            // failed to parse the token as Combination. Must be something else
        }
        return super.getDynamic(token, req, rsp);
    }

    @Override
    public void run() {
        run(new RunnerImpl());
    }

    @Override
    public Fingerprint.RangeSet getDownstreamRelationship(AbstractProject that) {
        Fingerprint.RangeSet rs = super.getDownstreamRelationship(that);
        for (MatrixRun run : getRuns()) {
            rs.add(run.getDownstreamRelationship(that));
        }
        return rs;
    }

    private class RunnerImpl extends AbstractRunner {
        private final List<MatrixAggregator> aggregators = new ArrayList<MatrixAggregator>();

        protected Result doRun(BuildListener listener) throws Exception {
            MatrixProject p = getProject();
            PrintStream logger = listener.getLogger();

            // list up aggregators
            for (Publisher pub : p.getPublishers().values()) {
                if (pub instanceof MatrixAggregatable) {
                    MatrixAggregatable ma = (MatrixAggregatable) pub;
                    MatrixAggregator a = ma.createAggregator(MatrixBuild.this, launcher, listener);
                    if (a != null) {
                        aggregators.add(a);
                    }
                }
            }

            //let properties do their job
            for (JobProperty prop : p.getProperties().values()) {
                if (prop instanceof MatrixAggregatable) {
                    MatrixAggregatable ma = (MatrixAggregatable) prop;
                    MatrixAggregator a = ma.createAggregator(MatrixBuild.this, launcher, listener);
                    if (a != null) {
                        aggregators.add(a);
                    }
                }
            }

            axes = p.getAxes();
            Collection<MatrixConfiguration> activeConfigurations = p.getActiveConfigurations();
            final int n = getNumber();

            String touchStoneFilter = p.getTouchStoneCombinationFilter();
            Collection<MatrixConfiguration> touchStoneConfigurations = new HashSet<MatrixConfiguration>();
            Collection<MatrixConfiguration> delayedConfigurations = new HashSet<MatrixConfiguration>();
            for (MatrixConfiguration c : activeConfigurations) {
                AxisList axes = p.getAxes();
                String touchStoneCombinationFilter = p.getTouchStoneCombinationFilter();
                if (touchStoneFilter != null && c.getCombination()
                    .evalGroovyExpression(axes, touchStoneCombinationFilter)) {
                    touchStoneConfigurations.add(c);
                } else {
                    delayedConfigurations.add(c);
                }
            }

            for (MatrixAggregator a : aggregators) {
                if (!a.startBuild()) {
                    return Result.FAILURE;
                }
            }

            try {
                if (!p.isRunSequentially()) {
                    for (MatrixConfiguration c : touchStoneConfigurations) {
                        scheduleConfigurationBuild(logger, c);
                    }
                }

                Result r = Result.SUCCESS;
                for (MatrixConfiguration c : touchStoneConfigurations) {
                    if (p.isRunSequentially()) {
                        scheduleConfigurationBuild(logger, c);
                    }
                    Result buildResult = waitForCompletion(listener, c);
                    r = r.combine(buildResult);
                }

                if (p.getTouchStoneResultCondition() != null && r.isWorseThan(p.getTouchStoneResultCondition())) {
                    logger.printf("Touchstone configurations resulted in %s, so aborting...\n", r);
                    return r;
                }

                if (!p.isRunSequentially()) {
                    for (MatrixConfiguration c : delayedConfigurations) {
                        scheduleConfigurationBuild(logger, c);
                    }
                }

                for (MatrixConfiguration c : delayedConfigurations) {
                    if (p.isRunSequentially()) {
                        scheduleConfigurationBuild(logger, c);
                    }
                    Result buildResult = waitForCompletion(listener, c);
                    r = r.combine(buildResult);
                }

                return r;
            } catch (InterruptedException e) {
                logger.println("Aborted");
                return Result.ABORTED;
            } catch (AggregatorFailureException e) {
                return Result.FAILURE;
            } finally {
                // if the build was aborted in the middle. Cancel all the configuration builds.
                Queue q = Hudson.getInstance().getQueue();
                synchronized (q) {// avoid micro-locking in q.cancel.
                    for (MatrixConfiguration c : activeConfigurations) {
                        if (q.cancel(c)) {
                            logger.println(Messages.MatrixBuild_Cancelled(c.getDisplayName()));
                        }
                        MatrixRun b = c.getBuildByNumber(n);
                        if (b != null) {
                            Executor exe = b.getExecutor();
                            if (exe != null) {
                                logger.println(Messages.MatrixBuild_Interrupting(b.getDisplayName()));
                                exe.interrupt();
                            }
                        }
                    }
                }
            }
        }

        private Result waitForCompletion(BuildListener listener, MatrixConfiguration c)
            throws InterruptedException, IOException, AggregatorFailureException {
            String whyInQueue = "";
            long startTime = System.currentTimeMillis();

            // wait for the completion
            int appearsCancelledCount = 0;
            while (true) {
                MatrixRun b = c.getBuildByNumber(getNumber());

                // two ways to get beyond this. one is that the build starts and gets done,
                // or the build gets cancelled before it even started.
                Result buildResult = null;
                if (b != null && !b.isBuilding()) {
                    buildResult = b.getResult();
                }
                Queue.Item qi = c.getQueueItem();
                if (b == null && qi == null) {
                    appearsCancelledCount++;
                } else {
                    appearsCancelledCount = 0;
                }

                if (appearsCancelledCount >= 5) {
                    // there's conceivably a race condition in computating b and qi, as their computation
                    // are not synchronized. There are indeed several reports of Hudson incorrectly assuming
                    // builds being cancelled. See
                    // http://www.nabble.com/Master-slave-problem-tt14710987.html and also
                    // http://www.nabble.com/Anyone-using-AccuRev-plugin--tt21634577.html#a21671389
                    // because of this, we really make sure that the build is cancelled by doing this 5
                    // times over 5 seconds
                    listener.getLogger().println(Messages.MatrixBuild_AppearsCancelled(c.getDisplayName()));
                    buildResult = Result.ABORTED;
                }

                if (buildResult != null) {
                    for (MatrixAggregator a : aggregators) {
                        if (!a.endRun(b)) {
                            throw new AggregatorFailureException();
                        }
                    }
                    return buildResult;
                }

                if (qi != null) {
                    // if the build seems to be stuck in the queue, display why
                    String why = qi.getWhy();
                    if (!why.equals(whyInQueue) && System.currentTimeMillis() - startTime > 5000) {
                        listener.getLogger().println(c.getDisplayName() + " is still in the queue: " + why);
                        whyInQueue = why;
                    }
                }

                Thread.sleep(1000);
            }
        }

        private void scheduleConfigurationBuild(PrintStream logger, MatrixConfiguration c) {
            logger.println(Messages.MatrixBuild_Triggering(c.getDisplayName()));
            c.scheduleBuild(getAction(ParametersAction.class), new UpstreamCause(MatrixBuild.this));
        }

        public void post2(BuildListener listener) throws Exception {
            for (MatrixAggregator a : aggregators) {
                a.endBuild();
            }
        }

        @Override
        protected Lease decideWorkspace(Node n, WorkspaceList wsl) throws IOException, InterruptedException {
            String customWorkspace = getProject().getCustomWorkspace();
            if (customWorkspace != null) {
                // we allow custom workspaces to be concurrently used between jobs.
                return Lease.createDummyLease(n.getRootPath().child(getEnvironment(listener).expand(customWorkspace)));
            }
            return super.decideWorkspace(n, wsl);
        }

    }

    /**
     * A private exception to help maintain the correct control flow after extracting the 'waitForCompletion' method
     */
    private static class AggregatorFailureException extends Exception {
    }

}
