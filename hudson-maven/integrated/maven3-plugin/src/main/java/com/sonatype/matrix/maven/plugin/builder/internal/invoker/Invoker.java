/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal.invoker;

import java.rmi.MarshalledObject;

/**
 * InvocationHandler-like thing w/o the method reference and using pre-marshalled arguments.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public interface Invoker
{
    MarshalledObject invoke(MethodKey key, MarshalledObject[] args) throws Throwable;
}