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
package hudson.util;

import hudson.Functions;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.security.AuthorizationMatrixProperty;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hudsonci.api.model.ICascadingJob;
import org.hudsonci.api.model.IProjectProperty;
import org.hudsonci.model.project.property.AxisListProjectProperty;
import org.hudsonci.model.project.property.BaseProjectProperty;
import org.hudsonci.model.project.property.BooleanProjectProperty;
import org.hudsonci.model.project.property.CopyOnWriteListProjectProperty;
import org.hudsonci.model.project.property.DescribableListProjectProperty;
import org.hudsonci.model.project.property.ExternalProjectProperty;
import org.hudsonci.model.project.property.IntegerProjectProperty;
import org.hudsonci.model.project.property.LogRotatorProjectProperty;
import org.hudsonci.model.project.property.ResultProjectProperty;
import org.hudsonci.model.project.property.SCMProjectProperty;
import org.hudsonci.model.project.property.StringProjectProperty;
import org.hudsonci.model.project.property.TriggerProjectProperty;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Utility class for cascading functionality.
 * <p/>
 * Date: 10/25/11
 *
 * @author Nikita Levyankov
 */
public class CascadingUtil {

    /**
     * Returns job property by specified key.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.api.model.IProjectProperty} instance or null.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static IProjectProperty getProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, null);
    }

    /**
     * Returns StringProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.StringProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static StringProjectProperty getStringProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, StringProjectProperty.class);
    }

    /**
     * Returns BaseProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.BaseProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static BaseProjectProperty getBaseProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, BaseProjectProperty.class);
    }

    /**
     * Returns ExternalProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.ExternalProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static ExternalProjectProperty getExternalProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, ExternalProjectProperty.class);
    }

    /**
     * Returns CopyOnWriteListProjectProperty by specified key. If property doesn't exists,
     * it will be initialized and added to current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.CopyOnWriteListProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static CopyOnWriteListProjectProperty getCopyOnWriteListProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, CopyOnWriteListProjectProperty.class);
    }

    /**
     * Returns ResultProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.ResultProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static ResultProjectProperty getResultProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, ResultProjectProperty.class);
    }

    /**
     * Returns BooleanProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.BooleanProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static BooleanProjectProperty getBooleanProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, BooleanProjectProperty.class);
    }

    /**
     * Returns IntegerProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.IntegerProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static IntegerProjectProperty getIntegerProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, IntegerProjectProperty.class);
    }

    /**
     * Returns LogRotatorProjectProperty by specified key. If property doesn't exists, it will be initialized and added
     * to current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.LogRotatorProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static LogRotatorProjectProperty getLogRotatorProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, LogRotatorProjectProperty.class);
    }

    /**
     * Returns DescribableListProjectProperty by specified key. If property doesn't exists, it will be initialized and
     * added to current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.DescribableListProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static DescribableListProjectProperty getDescribableListProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, DescribableListProjectProperty.class);
    }

    /**
     * Returns AxisListProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.AxisListProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static AxisListProjectProperty getAxesListProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, AxisListProjectProperty.class);
    }

    /**
     * Returns SCMProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.SCMProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static SCMProjectProperty getScmProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, SCMProjectProperty.class);
    }

    /**
     * Returns TriggerProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.TriggerProjectProperty} instance.
     * @throws IllegalArgumentException if currentJob is null.
     */
    public static TriggerProjectProperty getTriggerProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, TriggerProjectProperty.class);
    }

    /**
     * Returns project property by specified key.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @param clazz required property class.
     * If class is not null and property was not found, property of given class will be created.
     * @return {@link org.hudsonci.api.model.IProjectProperty} instance or null.
     * @throws IllegalArgumentException if currentJob is null.
     */
    @SuppressWarnings("unchecked")
    public static <T extends IProjectProperty> T getProjectProperty(ICascadingJob currentJob, String key, Class<T> clazz) {
        if (currentJob == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        IProjectProperty t = (IProjectProperty) currentJob.getProjectProperties().get(key);
        if (null == t && null != clazz) {
            try {
                t = clazz.getConstructor(ICascadingJob.class).newInstance(currentJob);
                t.setKey(key);
                currentJob.putProjectProperty(key, t);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) t;
    }

    /**
     * Checks whether cascadingCandidate project can produce cycle cascading dependencies.
     *
     * @param cascadingCandidate candidate.
     * @param cascadingChildren children of given job.
     * @return false - if cyclic cascading dependency is not possible, true - otherwise.
     */
    @SuppressWarnings("unchecked")
    public static boolean hasCyclicCascadingLink(Job cascadingCandidate, Set<String> cascadingChildren) {
        if (null != cascadingCandidate && CollectionUtils.isNotEmpty(cascadingChildren)) {
            if (cascadingChildren.contains(cascadingCandidate.getName())) {
                return true;
            }
            for (String childName : cascadingChildren) {
                Job job = Functions.getItemByName(Hudson.getInstance().getAllItems(Job.class), childName);
                if (null != job && hasCyclicCascadingLink(cascadingCandidate, job.getCascadingChildrenNames())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recursively unlink specified project from cascading hierarchy.
     *
     * @param cascadingProject cascading project to start from.
     * @param projectToUnlink project that should be unlinked.
     * @return true if project was unlinked, false - if cascadingProject or projectToUnlink is Null
     * @throws java.io.IOException if cascading project couldn't be saved.
     */
    public static boolean unlinkProjectFromCascadingParents(ICascadingJob cascadingProject, String projectToUnlink)
        throws IOException {
        if (null != cascadingProject && null != projectToUnlink) {
            Job job = Functions.getItemByName(Hudson.getInstance().getAllItems(Job.class), projectToUnlink);
            if (null != job) {
                Set<String> set = new HashSet<String>(job.getCascadingChildrenNames());
                set.add(projectToUnlink);
                return unlinkProjectFromCascadingParents(cascadingProject, set);
            }
        }
        return false;
    }

    /**
     * Recursively unlink set of projects from cascading hierarchy.
     *
     * @param cascadingProject cascading project to start from.
     * @param projectsToUnlink projects that should be unlinked.
     * @return if project was unlinked
     * @throws java.io.IOException if cascading project couldn't be saved.
     */
    private static boolean unlinkProjectFromCascadingParents(ICascadingJob cascadingProject, Set<String> projectsToUnlink)
        throws IOException {
        if (null != cascadingProject && null != projectsToUnlink) {
            for (String toUnlink : projectsToUnlink) {
                cascadingProject.removeCascadingChild(toUnlink);
            }
            if (cascadingProject.hasCascadingProject()) {
                unlinkProjectFromCascadingParents(cascadingProject.getCascadingProject(), projectsToUnlink);
            }
            return true;
        }
        return false;
    }

    /**
     * Links cascading project to children project. Method updates all parent cascading projects starting
     * from the specified cascadingProject.
     *
     * @param cascadingProject cascadingProject.
     * @param childProjectName the name of child project name.
     * @throws java.io.IOException if cascading project couldn't be saved.
     */
    public static void linkCascadingProjectsToChild(ICascadingJob cascadingProject, String childProjectName) throws IOException {
        if (cascadingProject != null) {
            cascadingProject.addCascadingChild(childProjectName);
            if (cascadingProject.hasCascadingProject()) {
                linkCascadingProjectsToChild(cascadingProject.getCascadingProject(), childProjectName);
            }
        }
    }

    /**
     * Updates the name of the project in all children cascading references.
     * If this project uses some cascading parent, the name of this project will be renamed in the cascading children
     * collection of the cascading parent project.
     *
     * @param cascadingProject cascading project.
     * @param oldName old project name.
     * @param newName new project name.
     * @throws java.io.IOException if cascading project couldn't be saved.
     */
    public static void renameCascadingChildLinks(ICascadingJob cascadingProject, String oldName, String newName)
        throws IOException {
        if (cascadingProject != null) {
            cascadingProject.renameCascadingChildName(oldName, newName);
            if (cascadingProject.hasCascadingProject()) {
                renameCascadingChildLinks(cascadingProject.getCascadingProject(), oldName, newName);
            }
        }
    }

    /**
     * Updates the name of the project in all parent cascading references.
     * If this project is used as cascading parent, it's name will be renamed in all children projects.
     *
     * @param oldName old project name.
     * @param newName new project name.
     */
    public static void renameCascadingParentLinks(final String oldName, final String newName) {
        if (StringUtils.isBlank(newName) || StringUtils.isBlank(oldName)) {
            return;
        }
        for (Job job : Hudson.getInstance().getAllItems(Job.class)) {
            if (oldName.equals(job.getCascadingProjectName())) {
                job.renameCascadingProjectNameTo(newName);
            }
        }
    }

    /**
     * Returns possible cascading parents for current job, which are filtered by type and checked for avoidness cyclic
     * dependency
     *
     * @param type project type.
     * @param currentJob current job instance
     * @param <T> Item
     * @return list of cascading parents.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Item> List<Job> getCascadingParents(Class<T> type, Job currentJob) {
        Job currentParent = currentJob.getCascadingProject();
        if (type.isInstance(currentParent) && !currentParent.hasPermission(Item.READ)) {
            return Collections.EMPTY_LIST; // user can't see parent so don't let them change it
        }
        List<T> allItems = Hudson.getInstance().getAllItems(type);
        List<Job> result = new ArrayList<Job>(allItems.size());
        for (T item : allItems) {
            Job job = (Job) item;
            if (!StringUtils.equals(currentJob.getName(), job.getName())
                && !hasCyclicCascadingLink(job, currentJob.getCascadingChildrenNames())) {
                result.add(job);
            }
        }
        return result;
    }

    /**
     * Creates {@link ExternalProjectProperty} based on Descriptors collection, StaplerRequest and JSON resonse.
     *
     * @param req StaplerRequest
     * @param json JSONObject
     * @param descriptors list of descriptors
     * @param owner job to be updated.
     * @param <T> Describable
     * @throws Descriptor.FormException if any.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Describable<T>> void buildExternalProperties(StaplerRequest req, JSONObject json,
                                                                          List<Descriptor<T>> descriptors, Job owner)
        throws Descriptor.FormException {
        for (Descriptor d : descriptors) {
            String name = d.getJsonSafeClassName();
            ExternalProjectProperty<Describable> baseProperty = getExternalProjectProperty(owner, name);
            Describable describable = null;
            if (json.has(name)) {
                describable = d.newInstance(req, json.getJSONObject(name));
            }
            baseProperty.setValue(describable);
        }
    }

    /**
     * Sets trigger for job and all its children if necessary.
     *
     * @param job parentJob
     * @param descriptor trigger descriptor
     * @param key trigger property key
     * @param req stapler request
     * @param json submited json
     * @throws Descriptor.FormException if incorrect parameters
     */
    @SuppressWarnings("unchecked")
    public static void setChildrenTrigger(Job job, TriggerDescriptor descriptor, String key, StaplerRequest req,
                                          JSONObject json) throws Descriptor.FormException {
        TriggerProjectProperty property = CascadingUtil.getTriggerProjectProperty(job, key);
        if (property.getValue() != null) {
            property.getValue().stop();
        }
        Trigger trigger = null;
        if (json.has(key)) {
            trigger = descriptor.newInstance(req, json.getJSONObject(key));
            trigger.start(job, true);
        }
        property.setValue(trigger);
        Set<String> cascadingChildrenNames = job.getCascadingChildrenNames();
        if (null != cascadingChildrenNames) {
            for (String childName : cascadingChildrenNames) {
                Job childJob = (Job) Hudson.getInstance().getItem(childName);
                if (null != childJob && StringUtils.equals(job.getName(), childJob.getCascadingProjectName())) {
                    TriggerProjectProperty childProperty = CascadingUtil.getTriggerProjectProperty(childJob, key);
                    if (!childProperty.isOverridden()) {
                        setChildrenTrigger(childJob, descriptor, key, req, json);
                    } else if (!childProperty.allowOverrideValue(trigger, childProperty.getValue())) {
                        childProperty.setOverridden(false);
                    }
                }
            }
        }
    }

    /**
     * Sets parameterDefinitionProperties for current job. This method is recursively executed for cascading children
     * for setting valid {@link ParametersDefinitionProperty#owner} value.
     *
     * @param job job.
     * @param key parameter key,
     * @param parameterDefinitionProperties new properties to set.
     */
    @SuppressWarnings("unchecked")
    public static void setParameterDefinitionProperties(Job job,
                                                        String key,
                                                        CopyOnWriteList<ParametersDefinitionProperty> parameterDefinitionProperties) {
        CopyOnWriteListProjectProperty projectProperty = getCopyOnWriteListProjectProperty(job, key);
        CopyOnWriteList<ParametersDefinitionProperty> pdProperties
            = new CopyOnWriteList<ParametersDefinitionProperty>();
        //Create new instance for each parameter in order to set owner and use in cascading children.
        for (ParametersDefinitionProperty pdp : parameterDefinitionProperties) {
            ParametersDefinitionProperty copiedDefinitionProperty = new ParametersDefinitionProperty(
                new ArrayList<ParameterDefinition>(pdp.getParameterDefinitions()));
            copiedDefinitionProperty.setOwner((AbstractProject) job);
            pdProperties.add(copiedDefinitionProperty);
        }
        projectProperty.setValue(pdProperties);
        Set<String> cascadingChildrenNames = job.getCascadingChildrenNames();
        //Iterate through cascading children and recursively update property for each child.
        if (null != cascadingChildrenNames) {
            for (String childName : cascadingChildrenNames) {
                Job childJob = (Job) Hudson.getInstance().getItem(childName);
                //Check only direct children in order to avoid deep checking for properties overridden properties.
                if (null != childJob && StringUtils.equals(job.getName(), childJob.getCascadingProjectName())) {
                    CopyOnWriteListProjectProperty childProperty = getCopyOnWriteListProjectProperty(childJob, key);
                    //If child value is equal to parent - mark this value as unmodified.
                    if (!childProperty.allowOverrideValue(childProperty.getValue(), pdProperties)) {
                        childProperty.setOverridden(false);
                    } else if (!childProperty.isOverridden()) {
                        //If child property was not overridden, update this property and cascading children if any.
                        setParameterDefinitionProperties(childJob, key, parameterDefinitionProperties);
                    }
                }
            }
        }
    }

    /**
     * Checks whether JobProperty supports cascading.
     * Method skips {@link AuthorizationMatrixProperty} and {@link ParametersDefinitionProperty} classes.
     * {@link AuthorizationMatrixProperty} doesn't support cascading for now.
     * As for {@link ParametersDefinitionProperty} single instance doesn't support cascading, so, classes are
     * grouped into list of {@link ParametersDefinitionProperty} and whole list could be inherited or overridden.

     * @param d property descriptor.
     * @return true - if JobProperty could be used for cascading, false - otherwise.
     * @see #setParameterDefinitionProperties(hudson.model.Job, String, CopyOnWriteList)
     * @see hudson.model.Job#getParameterDefinitionProperties()
     */
    public static boolean isCascadableJobProperty(JobPropertyDescriptor d) {
        return !(d instanceof AuthorizationMatrixProperty.DescriptorImpl
            || d instanceof ParametersDefinitionProperty.DescriptorImpl);
    }
}
