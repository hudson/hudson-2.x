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

import java.io.IOException;

/**
 * FreeStyle project interface.
 * <p/>
 * Date: 9/15/11
 *
 * @author Nikita Levyankov
 */
public interface IFreeStyleProject extends IProject {

    /**
     * Returns user-specified workspace directory, or null if it's up to Hudson
     *
     * @return string representation of directory.
     * @throws IOException if any.
     */
    String getCustomWorkspace() throws IOException;

    /**
     * User-specified workspace directory, or null if it's up to Hudson.
     *
     * <p>
     * Normally a free-style project uses the workspace location assigned by its parent container,
     * but sometimes people have builds that have hard-coded paths (which can be only built in
     * certain locations. see http://www.nabble.com/Customize-Workspace-directory-tt17194310.html for
     * one such discussion.)
     *
     * <p>
     * This is not {@link java.io.File} because it may have to hold a path representation on another OS.
     *
     * <p>
     * If this path is relative, it's resolved against {@link hudson.model.Node#getRootPath()} on the node where
     * this workspace is prepared.
     * @param customWorkspace new custom workspace to set
     * @since 1.320
     * @throws IOException if any.
     */
    void setCustomWorkspace(String customWorkspace) throws IOException;
}
