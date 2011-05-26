/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.maven.eventspy_30;

import org.hudsonci.utils.common.TestAccessible;
import org.apache.maven.eventspy.internal.EventSpyDispatcher;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.lifecycle.internal.LifecycleDebugLogger;
import org.apache.maven.lifecycle.internal.ProjectBuildList;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Bridges {@link LifecycleDebugLogger} hooks to {@link org.apache.maven.eventspy.EventSpy} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Component(role=LifecycleDebugLogger.class)
public class LifecycleDebugLoggerImpl
    extends LifecycleDebugLogger
{
    @Requirement
    private EventSpyDispatcher dispatcher;

    /**
     * For Plexus.
     */
    @SuppressWarnings({"unused"})
    public LifecycleDebugLoggerImpl() {
        super();
    }

    /**
     * For testing.
     */
    @TestAccessible
    public LifecycleDebugLoggerImpl(final org.codehaus.plexus.logging.Logger logger) {
        super(logger);
    }

    public class ReactorPlanEvent
    {
        public final ProjectBuildList projectBuilds;

        public ReactorPlanEvent(final ProjectBuildList projectBuilds) {
            this.projectBuilds = checkNotNull(projectBuilds);
        }

        public void delegate() {
            LifecycleDebugLoggerImpl.super.debugReactorPlan(projectBuilds);
        }

        @Override
        public String toString() {
            return "ReactorPlanEvent{" +
                    "projectBuilds=" + projectBuilds +
                    '}';
        }
    }

    @Override
    public void debugReactorPlan(final ProjectBuildList projectBuilds) {
        dispatcher.onEvent(new ReactorPlanEvent(projectBuilds));
    }

    public class ProjectPlanEvent
    {
        public final MavenProject currentProject;

        public final MavenExecutionPlan executionPlan;

        public ProjectPlanEvent(final MavenProject currentProject, final MavenExecutionPlan executionPlan) {
            this.currentProject = checkNotNull(currentProject);
            this.executionPlan = checkNotNull(executionPlan);
        }

        public void delegate() {
            LifecycleDebugLoggerImpl.super.debugProjectPlan(currentProject, executionPlan);
        }

        @Override
        public String toString() {
            return "ProjectPlanEvent{" +
                    "currentProject=" + currentProject +
                    ", executionPlan=" + executionPlan +
                    '}';
        }
    }

    @Override
    public void debugProjectPlan(final MavenProject currentProject, final MavenExecutionPlan executionPlan) {
        dispatcher.onEvent(new ProjectPlanEvent(currentProject, executionPlan));
    }

    public class WeavePlanEvent
    {
        public final MavenSession session;

        public WeavePlanEvent(final MavenSession session) {
            this.session = checkNotNull(session);
        }

        public void delegate() {
            LifecycleDebugLoggerImpl.super.logWeavePlan(session);
        }

        @Override
        public String toString() {
            return "WeavePlanEvent{" +
                    "session=" + session +
                    '}';
        }
    }

    @Override
    public void logWeavePlan(final MavenSession session) {
        dispatcher.onEvent(new WeavePlanEvent(session));
    }
}
