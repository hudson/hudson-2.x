/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Jorg Heymans, Stephen Connolly,
 * Tom Huybrechts, Anton Kozak, Nikita Levyankov
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

import hudson.Util;
import hudson.diagnosis.OldDataMonitor;
import hudson.tasks.Fingerprinter;
import hudson.tasks.Maven;
import hudson.tasks.Maven.MavenInstallation;
import hudson.tasks.Maven.ProjectWithMaven;
import java.util.HashSet;
import java.util.Set;
import org.hudsonci.api.model.IProject;

/**
 * Buildable software project.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Project<P extends Project<P,B>,B extends Build<P,B>>
    extends BaseBuildableProject<P, B>
    implements SCMedItem, Saveable, ProjectWithMaven, BuildableItemWithBuildWrappers, IProject {

    /**
     * Creates a new project.
     */
    public Project(ItemGroup parent,String name) {
        super(parent, name);
    }

    public AbstractProject<?, ?> asProject() {
        return this;
    }

    @Override
    protected Set<ResourceActivity> getResourceActivities() {
        final Set<ResourceActivity> activities = new HashSet<ResourceActivity>();

        activities.addAll(super.getResourceActivities());
        activities.addAll(Util.filter(getBuildersList(), ResourceActivity.class));
        activities.addAll(Util.filter(getPublishersList(),ResourceActivity.class));
        activities.addAll(Util.filter(getBuildWrappersList(), ResourceActivity.class));

        return activities;
    }

    @Override
    public boolean isFingerprintConfigured() {
        return getPublishersList().get(Fingerprinter.class)!=null;
    }

    public MavenInstallation inferMavenInstallation() {
        Maven m = getBuildersList().get(Maven.class);
        if (m!=null)    return m.getMaven();
        return null;
    }

    /**
     * @deprecated since 2006-11-05.
     *      Left for legacy config file compatibility
     */
    @Deprecated
    private transient String slave;

    private Object readResolve() {
        if (slave != null) OldDataMonitor.report(this, "1.60");
        return this;
    }
}
