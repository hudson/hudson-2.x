/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal.invoker;

import com.sonatype.matrix.maven.eventspy.common.Callback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handles signaling {@link #onClose} when {@link Callback#close} is called.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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