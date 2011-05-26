/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.maven.plugin.builder;

import hudson.model.AbstractProject;
import hudson.model.Action;

import org.hudsonci.maven.plugin.ui.MavenProjectUI;
import org.kohsuke.stapler.StaplerProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the project's "Maven" link and delegates to {@link org.hudsonci.maven.plugin.ui.MavenProjectUI} for viewing the project's Maven details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenProjectAction
    implements Action, StaplerProxy
{
    private final AbstractProject<?,?> project;

    private MavenProjectUI ui;

    public MavenProjectAction(final AbstractProject<?, ?> project) {
        this.project = checkNotNull(project);
    }

    public AbstractProject<?,?> getProject() {
        return project;
    }

    public MavenProjectUI getTarget() {
        if (ui == null) {
            ui = new MavenProjectUI(this);
        }
        return ui;
    }

    // FIXME: Making invisible action until we have a project GWT view available to display

    public String getIconFileName() {
        //return getTarget().getIconFileName();
        return null;
    }

    public String getDisplayName() {
        //return getTarget().getDisplayName();
        return null;
    }

    public String getUrlName() {
        //return getTarget().getUrlName();
        return null;
    }
}
