/*********************************************************************************
 *
 * Copyright (c) 2010-2011, Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Sonatype, Inc.
 *     
 ********************************************************************************/  

package org.eclipse.hudson.inject.injecto.internal;

import org.eclipse.hudson.inject.injecto.Injectable;
import hudson.model.Describable;

/**
 * Make any class implementing {@link Describable} also implement {@link Injectable}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public aspect DescribableInjectionAspect
    extends InjectionAspectSupport
{
    declare parents:
        Describable+ implements Injectable;
}
