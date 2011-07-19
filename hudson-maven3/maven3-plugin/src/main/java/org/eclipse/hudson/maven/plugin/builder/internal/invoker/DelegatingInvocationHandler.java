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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handlers which delegates to another handler.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DelegatingInvocationHandler
    implements InvocationHandler
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final InvocationHandler delegate;

    public DelegatingInvocationHandler(final InvocationHandler delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    public InvocationHandler getDelegate() {
        return delegate;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert method != null;

        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        return getDelegate().invoke(proxy, method, args);
    }
}
