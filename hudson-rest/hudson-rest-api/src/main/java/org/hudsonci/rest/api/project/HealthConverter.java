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

package org.hudsonci.rest.api.project;

import javax.inject.Inject;

import org.hudsonci.rest.api.internal.ConverterSupport;

import org.hudsonci.rest.model.project.HealthDTO;
import hudson.model.HealthReport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts {@link HealthReport} to {@link HealthDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class HealthConverter
    extends ConverterSupport
{
    @Inject
    HealthConverter() {
        super();
    }

    /**
     *
     * @param source the source to convert
     *            what to convert
     * @throws NullPointerException
     *             if source is null
     * @return a converted HealthReport
     */
    public HealthDTO convert(final HealthReport source) {
        checkNotNull(source);

        log.trace("Converting: {}", source);

        HealthDTO target = new HealthDTO();
        target.setScore(source.getScore());
        target.setDescription(source.getDescription());

        return target;
    }
}
