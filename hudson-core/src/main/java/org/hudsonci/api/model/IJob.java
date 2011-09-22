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
package org.hudsonci.api.model;

import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.tasks.LogRotator;
import java.io.IOException;
import java.util.Map;

/**
 * Interface that represents Job.
 * <p/>
 * Date: 9/15/11
 *
 * @author Nikita Levyankov
 */
public interface IJob<T extends IJob> {

    /**
     * Returns cascading project name.
     *
     * @return cascading project name.
     */
    String getCascadingProjectName();

    /**
     * Returns selected cascading project.
     *
     * @return cascading project.
     */
    T getCascadingProject();

    /**
     * Returns job property by specified key.
     *
     * @param key key.
     * @param clazz IProperty subclass.
     * @return {@link org.hudsonci.api.model.IProperty} instance or null.
     * @throws java.io.IOException if any.
     */
    IProperty getProperty(String key, Class<? extends IProperty> clazz) throws IOException;

    /**
     * Checks whether current job is inherited from other project.
     *
     * @return boolean.
     */
    boolean hasCascadingProject();

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
     * @return true if this instance supports log rotation configuration.
     */
    boolean supportsLogRotator();

    /**
     * Gets all the job properties configured for this job.
     *
     * @return Map of properties.
     */
    Map<JobPropertyDescriptor, JobProperty<?>> getProperties();
}
