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

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AppointedNode;
import hudson.model.Fingerprint;
import hudson.model.JDK;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ProminentProjectAction;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.DescribableList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Interface that reflects common methods for AbstractProject model
 * <p/>
 * Date: 9/15/11
 *
 * @author Nikita Levyankov
 */
public interface IAbstractProject extends IJob {
    /**
     * Returns configured SCM for project,
     *
     * @return {@link SCM} instance
     */
    SCM getScm();

    /**
     * Sets scm value.
     *
     * @param scm {@link SCM}
     * @throws IOException if any.
     */
    void setScm(SCM scm) throws IOException;

    /**
     * Returns map of triggers.
     *
     * @return {@link Map}.
     */
    Map<TriggerDescriptor, Trigger> getTriggers();

    /**
     * @return list of {@link Trigger} elements.
     */
    List<Trigger<?>> getTriggersList();

    /**
     * @return describable list of trigger elements.
     */
    DescribableList<Trigger<?>, TriggerDescriptor> getTriggerDescribableList();

    /**
     * Gets the specific trigger, should be null if the property is not configured for this job.
     *
     * @param clazz class of trigger
     * @return T
     */
    <T extends Trigger> T getTrigger(Class<T> clazz);

    /**
     * Sets list of triggers.
     *
     * @param triggerList list of {@link Trigger} object
     */
    void setTriggers(List<Trigger<?>> triggerList);

    /**
     * Adds a new {@link Trigger} to this {@link hudson.model.Project} if not active yet.
     *
     * @param trigger new trigger.
     * @throws IOException if any.
     */
    void addTrigger(Trigger<?> trigger) throws IOException;

    /**
     * Removes {@link Trigger} frin this {@link hudson.model.Project} by {@link TriggerDescriptor}.
     *
     * @param trigger descriptor of trigger.
     * @throws IOException if any.
     */
    void removeTrigger(TriggerDescriptor trigger) throws IOException;

    /**
     * Checks whether workspace should be cleaned before build
     *
     * @return boolean value
     */
    boolean isCleanWorkspaceRequired();

    /**
     * Sets cleanWorkspaceRequired flag.
     *
     * @param cleanWorkspaceRequired true - to always clean workspace.
     */
    void setCleanWorkspaceRequired(boolean cleanWorkspaceRequired);

    /**
     * Indicates whether build should be blocked while downstream project is building.
     *
     * @return true if yes, false if no, null - if value should be taken from parent
     */
    boolean blockBuildWhenDownstreamBuilding();

    /**
     * Sets blockBuildWhenDownstreamBuilding flag.
     *
     * @param b new boolean value.
     * @throws IOException if any.
     */
    void setBlockBuildWhenDownstreamBuilding(boolean b) throws IOException;

    /**
     * Indicates whether build should be blocked while upstream project is building.
     *
     * @return true if yes, false if no, null - if value should be taken from parent
     */
    boolean blockBuildWhenUpstreamBuilding();

    /**
     * Sets blockBuildWhenUpstreamBuilding flag.
     *
     * @param b new boolean value.
     * @throws IOException if any.
     */
    void setBlockBuildWhenUpstreamBuilding(boolean b) throws IOException;

    /**
     * Returns scm checkout retry count.
     *
     * @return int value.
     */
    int getScmCheckoutRetryCount();

    /**
     * Returns project quiet period.
     *
     * @return int value.
     */
    int getQuietPeriod();

    /**
     * Sets the custom quiet period of this project, or revert to the global default if null is given.
     *
     * @param seconds quiet period
     * @throws IOException if any.
     */
    void setQuietPeriod(Integer seconds) throws IOException;

    /**
     * If this project is configured to be always built on this node,
     * return that {@link hudson.model.Node}. Otherwise null.
     *
     * @return {@link hudson.model.Label} instance.
     */
    Label getAssignedLabel();

    /**
     * Gets the textual representation of the assigned label as it was entered by the user.
     *
     * @return string
     */
    String getAssignedLabelString();

    /**
     * Sets the assigned label.
     *
     * @param label node label.
     * @throws java.io.IOException exception.
     */
    void setAssignedLabel(Label label) throws IOException;

    /**
     * Assigns this job to the given node. A convenience method over {@link #setAssignedLabel(Label)}.
     *
     * @param node node.
     * @throws java.io.IOException exception
     */
    void setAssignedNode(Node node) throws IOException;

    /**
     * Gets whether this project is using the advanced affinity chooser UI.
     *
     * @return true - advanced chooser, false - simple textfield.
     */
    boolean isAdvancedAffinityChooser();

    /**
     * Sets whether this project is using the advanced affinity chooser UI.
     *
     * @param b true - advanced chooser, false - otherwise
     * @throws java.io.IOException exception.
     */
    void setAdvancedAffinityChooser(boolean b) throws IOException;

    /**
     * Sets {@link hudson.model.AppointedNode}.
     *
     * @param appointedNode {@link hudson.model.AppointedNode}.
     */
    void setAppointedNode(AppointedNode appointedNode);

    /**
     * Returns {@link AppointedNode}. Returned value is not null.
     *
     * @return appointedNode {@link AppointedNode}.
     */
    AppointedNode getAppointedNode();

    /**
     * Returns the root project value.
     *
     * @return the root project value.
     */
    AbstractProject getRootProject();

    /**
     * Gets a workspace for some build of this project.
     * <p/>
     * <p/>
     * This is useful for obtaining a workspace for the purpose of form field validation, where exactly
     * which build the workspace belonged is less important. The implementation makes a cursory effort
     * to find some workspace.
     *
     * @return null if there's no available workspace.
     * @since 1.319
     */
    FilePath getSomeWorkspace();

    /**
     * Gets some build that has a live workspace.
     *
     * @return null if no such build exists.
     */
    <R extends AbstractBuild> R getSomeBuildWithWorkspace();

    /**
     * Used in <tt>sidepanel.jelly</tt> to decide whether to display
     * the config/delete/build links.
     *
     * @return true - if configurable, false - otherwise.
     */
    boolean isConfigurable();

    /**
     * @return true if project disabled, false - otherwise.
     */
    boolean isDisabled();

    /**
     * Marks the build as disabled.
     *
     * @param b true - to disable project, false - enable.
     * @throws IOException if any.
     */
    void makeDisabled(boolean b) throws IOException;

    /**
     * @return list of {@link ProminentProjectAction}s for current project.
     */
    List<ProminentProjectAction> getProminentActions();

    /**
     * @return true if project is parameterized.
     */
    boolean isParameterized();

    /**
     * Cleans project workspace.
     *
     * @return true if success, false otherwise.
     * @throws IOException          if any.
     * @throws InterruptedException if any.
     */
    boolean cleanWorkspace() throws IOException, InterruptedException;

    /**
     * @return name of jdk chosen for current project. Could taken from parent
     */
    String getJDKName();

    /**
     * @return JDK that this project is configured with, or null.
     */
    JDK getJDK();

    /**
     * Overwrites the JDK setting.
     *
     * @param jdk new jdk name value.
     */
    void setJDK(String jdk);

    /**
     * Overwrites the JDK setting.
     *
     * @param jdk new {@link JDK} candidate
     * @throws IOException if any.
     */
    void setJDK(JDK jdk) throws IOException;

    /**
     * @return the other {@link AbstractProject}s that should be built when a build of this project is completed.
     */
    List<AbstractProject> getDownstreamProjects();

    /**
     * @return the other {@link AbstractProject}s that should be built before a build of this project is started.
     */
    List<AbstractProject> getUpstreamProjects();

    /**
     * Returns only those upstream projects that defines {@link hudson.tasks.BuildTrigger} to this project.
     * This is a subset of {@link #getUpstreamProjects()}
     *
     * @return A List of upstream projects that has a {@link hudson.tasks.BuildTrigger} to this project.
     */
    List<AbstractProject> getBuildTriggerUpstreamProjects();

    /**
     * @return all the upstream projects including transitive upstream projects.
     * @since 1.138
     */
    Set<AbstractProject> getTransitiveUpstreamProjects();

    /**
     * @return all the downstream projects including transitive downstream projects.
     * @since 1.138
     */
    Set<AbstractProject> getTransitiveDownstreamProjects();

    /**
     * Gets the dependency relationship map between this project (as the source)
     * and that project (as the sink.)
     *
     * @param that {@link AbstractProject} to find relations.
     * @return can be empty but not null. build number of this project to the build
     *         numbers of that project.
     */
    SortedMap<Integer, Fingerprint.RangeSet> getRelationship(AbstractProject that);
}
