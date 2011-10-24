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
package hudson.model;

import org.hudsonci.api.model.IProjectProperty;

/**
 * Mock class for FreeStyleProject
 * <p/>
 * Date: 9/27/11
 *
 * @author Nikita Levyankov
 */
public class FreeStyleProjectMock extends FreeStyleProject {

    //TODO find better solution
    /**
     * The name of the cascadingProject.
     */
    private String cascadingProjectName;


    public FreeStyleProjectMock(String name) {
        super((ItemGroup) null, name);
        setAllowSave(false);
    }

    @Override
    protected void updateTransientActions() {
    }

    /**
     * For the unit tests only. Sets cascadingProject for the job.
     *
     * @param cascadingProject parent job
     */
    public void setCascadingProject(FreeStyleProject cascadingProject) {
        this.cascadingProject = cascadingProject;
        this.cascadingProjectName = cascadingProject != null ? cascadingProject.getName() : null;
    }

    public String getCascadingProjectName() {
        return cascadingProjectName;
    }

    public void renameCascadingProjectNameTo(String cascadingProjectName) {
        this.cascadingProjectName = cascadingProjectName;
    }

    /**
     * Increase visibility for testing,
     *
     * @param key key.
     * @param property property instance.
     */
    public void putJobProperty(String key, IProjectProperty property) {
        super.putJobProperty(key, property);
    }
}
