/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal.invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handles calls to {@link Object} methods locally on the delegate instance.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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