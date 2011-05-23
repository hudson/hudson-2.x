/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder;

import com.sonatype.matrix.MetaProject;
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
 * @since 1.1
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