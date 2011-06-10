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

package org.hudsonci.service.internal;

import hudson.model.Hudson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base Hudson Service from which all service implementations should extend.
 *
 * <p>Common {@literal preconditions} of service implementation methods
 * include:
 *
 * <ul>
 * <li>throw a {@link NullPointerException} if a null object
 * reference is passed in any parameter.
 * <li>throw an {@link org.acegisecurity.AccessDeniedException} if the current thread context does not hold a required authority
 * to perform an operation
 * </ul>
 *
 * @author plynch
 * @since 2.1.0
 */
public abstract class ServiceSupport
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Hudson hudson;

    @Inject
    public void setHudson(final Hudson hudson) {
        this.hudson = checkNotNull(hudson);
    }

    protected Hudson getHudson()
    {
        return hudson;
    }
}
