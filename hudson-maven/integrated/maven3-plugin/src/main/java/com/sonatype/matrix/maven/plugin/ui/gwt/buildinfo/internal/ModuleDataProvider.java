/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;

import javax.inject.Singleton;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Strongly typed data provider for {@link MavenProjectDTO}s.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Singleton
public class ModuleDataProvider extends ListDataProvider<MavenProjectDTO>
{
    public static ProvidesKey<MavenProjectDTO> KEY_PROVIDER = new ProvidesKey<MavenProjectDTO>()
    {
        @Override
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
            @Override
            public boolean apply(MavenProjectDTO input) {
                return moduleId.equals(input.getId());
            }
        });
    }
}
