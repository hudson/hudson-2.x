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

package org.hudsonci.maven.plugin.builder.internal.invoker;

import org.hudsonci.maven.eventspy.common.Callback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handles signaling {@link #onClose} when {@link Callback#close} is called.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class CallbackCloseAwareHandler
    extends DelegatingInvocationHandler
{
    private static final Method CLOSE_METHOD;

    static {
        try {
            CLOSE_METHOD = Callback.class.getMethod("close");
        }
        catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    public CallbackCloseAwareHandler(final InvocationHandler delegate) {
        super(delegate);
    }

    protected abstract void onClose();

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert method != null;

        try {
            return getDelegate().invoke(proxy, method, args);
        }
        finally {
            if (method.equals(CLOSE_METHOD)) {
                onClose();
            }
        }
    }
}
