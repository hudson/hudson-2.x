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

import com.google.inject.Key;
import com.sonatype.matrix.smoothie.Smoothie;
import com.sonatype.matrix.smoothie.injecto.Injectomatic;
import com.sonatype.matrix.smoothie.injecto.InjectomaticAware;
import com.sonatype.matrix.smoothie.internal.OID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for the {@link Injectomatic} aspect.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class InjectomaticAspectHelper
{
    private static final Logger log = LoggerFactory.getLogger(InjectomaticAspectHelper.class);

    private static boolean enabled = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(final boolean flag) {
        enabled = flag;
        log.debug("Aspect-based injection {}", flag ? "enabled" : "disabled");
    }

    private static Injectomatic injecto;

    private static Injectomatic getInjectomatic() {
        if (injecto == null) {
            injecto = Smoothie.getContainer().get(Key.get(Injectomatic.class));
        }
        return injecto;
    }

    // Used by aspect
    static void inject(final Object object) {
        assert object != null;

        if (!enabled) {
            log.warn("Injection disabled; ignoring request to inject: {}", OID.get(object));
            return;
        }

        getInjectomatic().inject(object);
    }

    // Used by aspect
    static void install(final Object object) {
        assert object != null;

        if (!enabled) {
            log.warn("Injection disabled; ignoring request to install injectomatic: {}", OID.get(object));
            return;
        }

        ((InjectomaticAware)object).setInjectomatic(getInjectomatic());
    }
}