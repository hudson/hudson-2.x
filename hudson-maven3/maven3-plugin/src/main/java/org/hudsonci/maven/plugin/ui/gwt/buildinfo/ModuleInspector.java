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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo;

import org.hudsonci.maven.model.state.BuildResultDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the rich domain logic for the {@link MavenProjectDTO}.
 * 
 * May be used by GWT so ensure that only translatable classes are used.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ModuleInspector
{
    private static final Collection<BuildResultDTO> completedStates = Arrays.asList(BuildResultDTO.SUCCESS, BuildResultDTO.FAILURE);

    /**
     * TODO: make duration a Long then it can be null to indicate there is no duration.
     */
    public static boolean hasDuration(MavenProjectDTO module) {
        return completedStates.contains(module.getBuildSummary().getResult());
    }
}
