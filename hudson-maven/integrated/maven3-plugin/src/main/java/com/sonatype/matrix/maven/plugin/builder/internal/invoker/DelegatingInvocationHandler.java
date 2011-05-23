/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal.invoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handlers which delegates to another handler.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert method != null;

        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        return getDelegate().invoke(proxy, method, args);
    }
}