/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Anton Kozak
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
package org.hudsonci.api.matrix;

import hudson.matrix.AxisList;
import hudson.model.Descriptor;
import org.hudsonci.api.model.IAbstractProject;
import hudson.model.Result;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Matrix Project Interface.
 *
 * @author Anton Kozak
 */
public interface IMatrixProject extends IAbstractProject {

    /**
     * Returns {@link hudson.matrix.AxisList} of configured axes.
     *
     * @return {@link hudson.matrix.AxisList} of configured axes.
     */
    public AxisList getAxes();

    /**
     * Reconfigures axes.
     *
     * @param axes new {@link AxisList}.
     * @throws java.io.IOException exception.
     */
    public void setAxes(AxisList axes) throws IOException;

    /**
     * Whether Hudson should run {@link hudson.matrix.MatrixRun}s are run sequentially.
     *
     * @return If true, {@link hudson.matrix.MatrixRun}s are run sequentially, instead of running in parallel.
     */
    boolean isRunSequentially();

    /**
     * Sets the mode of the running.
     *
     * @param runSequentially If true, {@link hudson.matrix.MatrixRun}s are run sequentially, instead of running in parallel.
     * @throws IOException exception.
     */
    void setRunSequentially(boolean runSequentially) throws IOException;

    /**
     * Sets the combination filter.
     *
     * @param combinationFilter the combinationFilter to set
     * @throws java.io.IOException exception.
     */
    void setCombinationFilter(String combinationFilter) throws IOException;

    /**
     * Obtains the combination filter, used to trim down the size of the matrix.
     * <p/>
     * <p/>
     * By default, a {@link hudson.matrix.MatrixConfiguration} is created for every possible combination of axes exhaustively.
     * But by specifying a Groovy expression as a combination filter, one can trim down the # of combinations built.
     * <p/>
     * <p/>
     * Namely, this expression is evaluated for each axis value combination, and only when it evaluates to true,
     * a corresponding {@link hudson.matrix.MatrixConfiguration} will be created and built.
     *
     * @return can be null.
     * @since 1.279
     */
    String getCombinationFilter();

    /**
     * Returns touchstone combination filter.
     *
     * @return touchstone combination filter.
     */
    String getTouchStoneCombinationFilter();

    /**
     * Sets touchstone combination filter.
     *
     * @param touchStoneCombinationFilter touchstone combination filter.
     */
    void setTouchStoneCombinationFilter(String touchStoneCombinationFilter);

    /**
     * Returns touchstone combination result condition.
     *
     * @return touchstone combination result condition.
     */
    Result getTouchStoneResultCondition();

    /**
     * Sets touchstone combination result condition.
     *
     * @param touchStoneResultCondition touchstone combination result condition.
     */
    void setTouchStoneResultCondition(Result touchStoneResultCondition);

    /**
     * Returns custom workspace.
     *
     * @return custom workspace.
     */
    String getCustomWorkspace();

    /**
     * Sets User-specified workspace directory, or null if it's up to Hudson.
     * <p/>
     * <p/>
     * Normally a matrix project uses the workspace location assigned by its parent container,
     * but sometimes people have builds that have hard-coded paths.
     * <p/>
     * <p/>
     * This is not {@link java.io.File} because it may have to hold a path representation on another OS.
     * <p/>
     * <p/>
     * If this path is relative, it's resolved against {@link hudson.model.Node#getRootPath()} on the node where this workspace
     * is prepared.
     *
     * @param customWorkspace custom workspace.
     * @throws java.io.IOException exception.
     */
    void setCustomWorkspace(String customWorkspace) throws IOException;

    /**
     * Returns list of active {@link Builder}s configured for this project.
     *
     * @return list of active {@link Builder}s configured for this project.
     */
    List<Builder> getBuilders();

    /**
     * Returns map of  active {@link Publisher} configured for this project.
     *
     * @return map of active {@link Publisher} configured for this project.
     */
    Map<Descriptor<Publisher>, Publisher> getPublishers();

    /**
     * Returns map of  active {@link BuildWrapper} configured for this project.
     *
     * @return map of  active {@link BuildWrapper} configured for this project.
     */
    Map<Descriptor<BuildWrapper>,BuildWrapper> getBuildWrappers();

}
