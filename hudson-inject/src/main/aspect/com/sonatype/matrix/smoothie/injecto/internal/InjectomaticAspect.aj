/**
 * The MIT License
 *
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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

package com.sonatype.matrix.smoothie.injecto.internal;

import com.sonatype.matrix.smoothie.injecto.InjectomaticAware;
import com.sonatype.matrix.smoothie.injecto.internal.InjectomaticAspectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles magic injection of objects.
 *
 * Currently supports:
 * <ul>
 * <li>{@link com.sonatype.matrix.smoothie.injecto.InjectomaticAware}
 * <li>{@link com.sonatype.matrix.smoothie.injecto.Injectable}
 * <li>{@link hudson.model.Describable}
 * </ul>
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
public privileged aspect InjectomaticAspect
{
    private static final Logger log = LoggerFactory.getLogger(InjectomaticAspect.class);

    public InjectomaticAspect() {
        log.debug("Aspect created: {}", this);
    }

    /**
     * Handle InjectomaticAware.  Catches constructor calls for types implementing InjectomaticAware to
     * to set the Injectomatic instance on the target object.  This is used for non-JSR-330 components
     * that need to have access to the injecto system.
     */
    after():
        execution(com.sonatype.matrix.smoothie.injecto.InjectomaticAware+.new(..))
    {
        Object target = thisJoinPoint.getThis();
        log.trace("Found injecto-aware: {}", target);
        InjectomaticAspectHelper.install(target);
    }

    //
    // In a nutshell this catches constructor calls for Describale and Injectable types
    // and requests that the constructed object receive injection.
    //

    /**
     * Inject Describable objects after they have been created.
     */
    after():
        execution(public hudson.model.Describable+.new(..))
    {
        log.trace("Injecting construction of Describable from aspect");
        Object target = thisJoinPoint.getThis();
        InjectomaticAspectHelper.inject(target);
    }

    /**
     * Handle magical injection of any type implementing Injectable.
     */
    after():
        execution(com.sonatype.matrix.smoothie.injecto.Injectable+.new(..))
    {
        log.trace("Injecting construction of Injectable from aspect");
        Object target = thisJoinPoint.getThis();
        InjectomaticAspectHelper.inject(target);
    }
}