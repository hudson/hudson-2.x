/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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

import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for {@link hudson.model.BaseBuildableProject}.
 * <p/>
 * Date: 11/25/11
 *
 * @author Nikita Levyankov
 */
public interface IBaseBuildableProject extends IAbstractProject {
    /**
     * @return list of project {@link hudson.tasks.Builder}
     */
    List<Builder> getBuilders();

    DescribableList<Builder, Descriptor<Builder>> getBuildersList();

    void setBuilders(DescribableList<Builder, Descriptor<Builder>> builders);

    /**
     * @return map of project {@link hudson.tasks.BuildWrapper}
     */
    Map<Descriptor<BuildWrapper>, BuildWrapper> getBuildWrappers();

    /**
     * @return map of project {@link hudson.tasks.Publisher}
     */
    Map<Descriptor<Publisher>, Publisher> getPublishers();

    Publisher getPublisher(Descriptor<Publisher> descriptor);

    /**
     * Adds a new {@link hudson.tasks.BuildStep} to this {@link IBaseBuildableProject} and saves the configuration.
     *
     * @param publisher publisher.
     * @throws java.io.IOException exception.
     */
    void addPublisher(Publisher publisher) throws IOException;

    /**
     * Removes a publisher from this project, if it's active.
     *
     * @param publisher publisher.
     * @throws java.io.IOException exception.
     */
    void removePublisher(Descriptor<Publisher> publisher) throws IOException;
}
