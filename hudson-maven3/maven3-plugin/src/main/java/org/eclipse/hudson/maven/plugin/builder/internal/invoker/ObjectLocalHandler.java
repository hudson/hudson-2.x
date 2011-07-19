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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handles calls to {@link Object} methods locally on the delegate instance.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ObjectLocalHandler
    extends DelegatingInvocationHandler
{
    public ObjectLocalHandler(final InvocationHandler delegate) {
        super(delegate);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert method != null;

        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(getDelegate(), args);
        }

        return getDelegate().invoke(proxy, method, args);
    }
}
