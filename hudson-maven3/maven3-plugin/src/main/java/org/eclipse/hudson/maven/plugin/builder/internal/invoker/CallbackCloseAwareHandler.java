/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.maven.plugin.builder.internal.invoker;

import org.model.hudson.maven.eventspy.common.Callback;

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
