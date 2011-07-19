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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import javax.inject.Singleton;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Strongly typed data provider for {@link MavenProjectDTO}s.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ModuleDataProvider extends ListDataProvider<MavenProjectDTO>
{
    public static ProvidesKey<MavenProjectDTO> KEY_PROVIDER = new ProvidesKey<MavenProjectDTO>()
    {
        public Object getKey(final MavenProjectDTO module) {
            return module != null ? module.getId() : null;
        }
    };

    public ModuleDataProvider() {
        super(KEY_PROVIDER);
    }
    
    /**
     * @throws NoSuchElementException if no matching moduleId is found
     */
    public MavenProjectDTO find(final String moduleId) throws NoSuchElementException
    {
        checkNotNull(moduleId);

        return Iterables.find(this.getList(), new Predicate<MavenProjectDTO>()
        {
            public boolean apply(MavenProjectDTO input) {
                return moduleId.equals(input.getId());
            }
        });
    }
}
