/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Nikita Levyankov
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
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hudsonci.api.model.IJob;
import org.hudsonci.api.model.IProjectProperty;
import org.hudsonci.model.project.property.AxisListProjectProperty;
import org.hudsonci.model.project.property.BaseProjectProperty;
import org.hudsonci.model.project.property.BooleanProjectProperty;
import org.hudsonci.model.project.property.DescribableListProjectProperty;
import org.hudsonci.model.project.property.ExternalProjectProperty;
import org.hudsonci.model.project.property.IntegerProjectProperty;
import org.hudsonci.model.project.property.LogRotatorProjectProperty;
import org.hudsonci.model.project.property.ResultProjectProperty;
import org.hudsonci.model.project.property.SCMProjectProperty;
import org.hudsonci.model.project.property.StringProjectProperty;

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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
     */
    public static ExternalProjectProperty getExternalProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, ExternalProjectProperty.class);
    }

    /**
     * Returns ResultProjectProperty by specified key. If property doesn't exists, it will be initialized and added to
     * current job.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @return {@link org.hudsonci.model.project.property.ResultProjectProperty} instance.
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
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
     * @throws NullPointerException if currentJob is null.
     */
    public static SCMProjectProperty getScmProjectProperty(Job currentJob, String key) {
        return getProjectProperty(currentJob, key, SCMProjectProperty.class);
    }

    /**
     * Returns project property by specified key.
     *
     * @param currentJob job that should be analyzed.
     * @param key key.
     * @param clazz required property class.
     * If class is not null and property was not found, property of given class will be created.
     * @return {@link org.hudsonci.api.model.IProjectProperty} instance or null.
     * @throws NullPointerException if currentJob is null.
     */
    @SuppressWarnings("unchecked")
    public static <T extends IProjectProperty> T getProjectProperty(Job currentJob, String key, Class<T> clazz) {
        IProjectProperty t = (IProjectProperty) currentJob.getProjectProperties().get(key);
        if (null == t && null != clazz) {
            try {
                t = clazz.getConstructor(IJob.class).newInstance(currentJob);
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
                if (hasCyclicCascadingLink(cascadingCandidate, job.getCascadingChildrenNames())) {
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
     */
    public static boolean unlinkProjectFromCascadingParents(Job cascadingProject, String projectToUnlink) {
        if (null != cascadingProject && null != projectToUnlink) {
            cascadingProject.removeCascadingChild(projectToUnlink);
            if (cascadingProject.hasCascadingProject()) {
                unlinkProjectFromCascadingParents(cascadingProject.getCascadingProject(), projectToUnlink);
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
     */
    public static void linkCascadingProjectsToChild(Job cascadingProject, String childProjectName) {
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
     */
    public static void renameCascadingChildLinks(Job cascadingProject, String oldName, String newName) {
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
        List<T> allItems = Hudson.getInstance().getAllItems(type);
        List<Job> result = new ArrayList<Job>(allItems.size());
        for (T item : allItems) {
            Job job = (Job) item;
            if (!hasCyclicCascadingLink(job, currentJob.getCascadingChildrenNames())) {
                result.add(job);
            }
        }
        return result;
    }

}
