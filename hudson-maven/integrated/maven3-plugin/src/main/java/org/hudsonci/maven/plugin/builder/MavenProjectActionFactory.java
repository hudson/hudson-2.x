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

import org.hudsonci.utils.tasks.MetaProject;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.tasks.Builder;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;

/**
 * Attaches the single {@link MavenProjectAction} instance to each supportable project (freestyle, multiconfig)
 * which have at least one {@link MavenBuilder} configured.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
@Typed(TransientProjectActionFactory.class)
public class MavenProjectActionFactory
    extends TransientProjectActionFactory
{
    @Override
    public Collection<? extends Action> createFor(final AbstractProject project) {
        assert project != null;
        MetaProject meta = new MetaProject(project);
        if (meta.isSupported()) {
            for (Builder builder : meta.getBuilders()) {
                if (builder instanceof MavenBuilder) {
                    return Collections.singleton(new MavenProjectAction(project));
                }
            }
        }
        return Collections.emptySet();
    }
}
