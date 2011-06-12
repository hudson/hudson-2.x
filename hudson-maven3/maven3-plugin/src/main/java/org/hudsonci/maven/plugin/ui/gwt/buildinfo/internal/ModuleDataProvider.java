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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import org.hudsonci.maven.model.state.MavenProjectDTO;

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
