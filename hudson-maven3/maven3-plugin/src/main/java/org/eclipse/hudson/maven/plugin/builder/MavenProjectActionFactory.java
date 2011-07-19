/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.maven.plugin.builder;

import org.eclipse.hudson.utils.tasks.MetaProject;

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
