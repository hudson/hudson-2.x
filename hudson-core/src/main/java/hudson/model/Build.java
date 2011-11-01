/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Martin Eigenbrodt, Anton Kozak
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
package hudson.model;

import hudson.tasks.BuildStep;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Recorder;
import hudson.tasks.Notifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static hudson.model.Result.FAILURE;
import static hudson.model.Result.ABORTED;
/**
 * A build of a {@link Project}.
 *
 * <h2>Steps of a build</h2>
 * <p>
 * Roughly speaking, a {@link Build} goes through the following stages:
 *
 * <dl>
 * <dt>SCM checkout
 * <dd>Hudson decides which directory to use for a build, then the source code is checked out
 *
 * <dt>Pre-build steps
 * <dd>Everyone gets their {@link BuildStep#prebuild(AbstractBuild, BuildListener)} invoked
 * to indicate that the build is starting
 *
 * <dt>Build wrapper set up
 * <dd>{@link BuildWrapper#setUp(AbstractBuild, Launcher, BuildListener)} is invoked. This is normally
 * to prepare an environment for the build.
 *
 * <dt>Builder runs
 * <dd>{@link Builder#perform(AbstractBuild, Launcher, BuildListener)} is invoked. This is where
 * things that are useful to users happen, like calling Ant, Make, etc.
 *
 * <dt>Recorder runs
 * <dd>{@link Recorder#perform(AbstractBuild, Launcher, BuildListener)} is invoked. This is normally
 * to record the output from the build, such as test results.
 *
 * <dt>Notifier runs
 * <dd>{@link Notifier#perform(AbstractBuild, Launcher, BuildListener)} is invoked. This is normally
 * to send out notifications, based on the results determined so far.
 * </dl>
 *
 * <p>
 * And beyond that, the build is considered complete, and from then on {@link Build} object is there to
 * keep the record of what happened in this build. 
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Build <P extends BaseBuildableProject<P,B>,B extends Build<P,B>>
    extends AbstractBuild<P,B> {

    /**
     * Creates a new build.
     */
    protected Build(P project) throws IOException {
        super(project);
    }

    protected Build(P job, Calendar timestamp) {
        super(job, timestamp);
    }

    /**
     * Loads a build from a log file.
     */
    protected Build(P project, File buildDir) throws IOException {
        super(project,buildDir);
    }

//
//
// actions
//
//
    @Override
    public void run() {
        run(createRunner());
    }

    protected Runner createRunner() {
        return new RunnerImpl();
    }
    
    protected class RunnerImpl extends AbstractRunner {
        protected Result doRun(BuildListener listener) throws Exception {
            if(!preBuild(listener,project.getBuilders()))
                return FAILURE;
            if(!preBuild(listener,project.getPublishers()))
                return FAILURE;

            Result r = null;
            try {
                List<BuildWrapper> wrappers = new ArrayList<BuildWrapper>(project.getBuildWrappers().values());
                
                ParametersAction parameters = getAction(ParametersAction.class);
                if (parameters != null)
                    parameters.createBuildWrappers(Build.this,wrappers);

                for( BuildWrapper w : wrappers ) {
                    Environment e = w.setUp((AbstractBuild<?,?>)Build.this, launcher, listener);
                    if(e==null)
                        return (r = FAILURE);
                    buildEnvironments.add(e);
                }

                if(!build(listener,project.getBuilders()))
                    r = FAILURE;
            } catch (InterruptedException e) {
                r = ABORTED;
                throw e;
            } finally {
                if (r != null) setResult(r);
                // tear down in reverse order
                boolean failed=false;
                for( int i=buildEnvironments.size()-1; i>=0; i-- ) {
                    if (!buildEnvironments.get(i).tearDown(Build.this,listener)) {
                        failed=true;
                    }                    
                }
                // WARNING The return in the finally clause will trump any return before
                if (failed) return FAILURE;
            }

            return r;
        }

        public void post2(BuildListener listener) throws IOException, InterruptedException {
            if (!performAllBuildSteps(listener, project.getPublishers(), true))
                setResult(FAILURE);
            if (!performAllBuildSteps(listener, project.getProperties(), true))
                setResult(FAILURE);
        }

        @Override
        public void cleanUp(BuildListener listener) throws Exception {
            // at this point it's too late to mark the build as a failure, so ignore return value.
            performAllBuildSteps(listener, project.getPublishers(), false);
            performAllBuildSteps(listener, project.getProperties(), false);
            super.cleanUp(listener);
        }

        private boolean build(BuildListener listener, Collection<Builder> steps) throws IOException, InterruptedException {
            for( BuildStep bs : steps )
                if(!perform(bs,listener))
                    return false;
            return true;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Build.class.getName());
}
