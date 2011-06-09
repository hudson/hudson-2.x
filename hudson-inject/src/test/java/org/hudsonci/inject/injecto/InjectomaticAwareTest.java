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

package org.hudsonci.inject.injecto;

import com.google.inject.Key;
import org.hudsonci.inject.Smoothie;
import org.hudsonci.inject.SmoothieContainer;
import org.hudsonci.inject.SmoothieTestSupport;
import org.junit.Test;

import javax.inject.Named;
import javax.inject.Singleton;

import static org.junit.Assert.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * Tests for {@link InjectomaticAware}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class InjectomaticAwareTest
    extends SmoothieTestSupport
{
    @Test
    public void testInjectoAwareWithImplements() {
        Injectomatic injecto = Smoothie.getContainer().get(Key.get(Injectomatic.class));
        assertNotNull(injecto);

        TestInjectomaticAware aware = Smoothie.getContainer().get(Key.get(TestInjectomaticAware.class));
        assertNotNull(aware.injecto);
        assertEquals(injecto, aware.injecto);
    }

    @Named
    @Singleton
    public static class TestInjectomaticAware
        implements InjectomaticAware
    {
        Injectomatic injecto;

        public void setInjectomatic(Injectomatic injecto) {
            this.injecto = injecto;
        }
    }

    @Test
    public void testInjectoAwareWithExtends() {
        SmoothieContainer container = Smoothie.getContainer();
        assertNotNull(container);

        Injectomatic injecto = container.get(Key.get(Injectomatic.class));
        assertNotNull(injecto);
        System.out.println(injecto);

        TestInjectomaticAware aware = container.get(Key.get(TestInjectomaticAware2.class));
        assertNotNull(aware.injecto);
        assertEquals(injecto, aware.injecto);
    }

    @Named
    @Singleton
    public static class TestInjectomaticAware2
        extends TestInjectomaticAware
    {
        public void setInjectomatic(Injectomatic injecto) {
            checkState(this.injecto == null);
            super.setInjectomatic(injecto);
        }
    }
}
