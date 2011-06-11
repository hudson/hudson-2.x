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

package org.hudsonci.rest.api.build;

import org.hudsonci.rest.model.build.CauseDTO;

import java.util.ArrayList;
import java.util.List;

import org.hudsonci.rest.api.internal.ConverterSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts {@link hudson.model.Cause} into {@link CauseDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class CauseConverter
    extends ConverterSupport
{
    public CauseDTO convert(final hudson.model.Cause source) {
        checkNotNull(source);

        log.trace("Converting: {}", source);

        CauseDTO target = new CauseDTO();
        target.setType(source.getClass().getName());
        target.setDescription(source.getShortDescription());
        // TODO: Render description.jelly view of source as target's detail

        return target;
    }

    public List<CauseDTO> convert(final List<hudson.model.Cause> source) {
        checkNotNull(source);

        List<CauseDTO> target = new ArrayList<CauseDTO>(source.size());

        for (hudson.model.Cause cause : source) {
            target.add(convert(cause));
        }

        return target;
    }
}
