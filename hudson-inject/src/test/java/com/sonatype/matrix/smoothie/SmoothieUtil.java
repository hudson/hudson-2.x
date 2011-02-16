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

package com.sonatype.matrix.smoothie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Smoothie test utilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class SmoothieUtil
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieUtil.class);

    /**
     * Uses reflection to install a container instance.  This allows the container instance to be reset.
     *
     * @param container     The container to install in {@link com.sonatype.matrix.smoothie.Smoothie} or null to reset to the default.
     */
    public static void installContainer(final SmoothieContainer container) throws NoSuchFieldException, IllegalAccessException {
        Field field = Smoothie.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        if (container == null) {
            log.info("Reset container");
        }
        else {
            log.info("Installed custom container: {}", container);
        }
    }
}