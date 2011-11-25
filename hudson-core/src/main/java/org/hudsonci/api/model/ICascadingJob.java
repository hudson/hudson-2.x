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

import hudson.model.Job;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Job interface that exposes cascading functionality
 * <p/>
 * Date: 11/25/11
 *
 * @author Nikita Levyankov
 */
public interface ICascadingJob<T extends Job<?,?>> extends IJob<T> {

    /**
     * Returns cascading project name.
     *
     * @return cascading project name.
     */
    String getCascadingProjectName();

    /**
     * Sets cascadingProject name and saves project configuration.
     *
     * @param cascadingProjectName cascadingProject name.
     * @throws java.io.IOException if configuration couldn't be saved.
     */
    void setCascadingProjectName(String cascadingProjectName) throws IOException;

    /**
     * Returns selected cascading project.
     *
     * @return cascading project.
     */
    ICascadingJob getCascadingProject();

    /**
     * Returns job property by specified key.
     *
     * @param key key.
     * @param clazz IProperty subclass.
     * @return {@link IProjectProperty} instance or null.
     */
    IProjectProperty getProperty(String key, Class<? extends IProjectProperty> clazz);

    /**
     * Returns job property by specified key.
     *
     * @param key key.
     * @return {@link org.hudsonci.api.model.IProjectProperty} instance or null.
     */
    IProjectProperty getProperty(String key);

    /**
     * Removes project property.
     *
     * @param key property key.
     */
    void removeProjectProperty(String key);

    /**
     * Put job property to properties map.
     *
     * @param key key.
     * @param property property instance.
     */
    void putProjectProperty(String key, IProjectProperty property);

    /**
     * @return project properties.
     */
    Map<String, IProjectProperty> getProjectProperties();

    /**
     * Checks whether current job is inherited from other project.
     *
     * @return boolean.
     */
    boolean hasCascadingProject();

    /**
     * Remove cascading child project name and saves job configuration
     *
     * @param oldChildName old child project name.
     * @param newChildName new child project name.
     * @throws java.io.IOException if configuration couldn't be saved.
     */
    void renameCascadingChildName(String oldChildName, String newChildName) throws IOException;

    /**
     * Checks whether job has cascading children with given name
     *
     * @param cascadingChildName name of child.
     * @return true if job has child with specified name, false - otherwise.
     */
    boolean hasCascadingChild(String cascadingChildName);

    /**
     * Remove cascading child project name and saves job configuration
     *
     * @param cascadingChildName cascading child project name.
     * @throws java.io.IOException if configuration couldn't be saved.
     */
    void removeCascadingChild(String cascadingChildName) throws IOException;

    /**
     * Adds cascading child project name and saves configuration.
     *
     * @param cascadingChildName cascading child project name.
     * @throws java.io.IOException if configuration couldn't be saved.
     */
    void addCascadingChild(String cascadingChildName) throws IOException;

    /**
     * @return list of cascading children project names.
     */
    Set<String> getCascadingChildrenNames();

    /**
     * Renames cascading project name. For the properties processing and children links updating
     * please use {@link #setCascadingProjectName} instead.
     *
     * @param cascadingProjectName new project name.
     */
    void renameCascadingProjectNameTo(String cascadingProjectName);
}
