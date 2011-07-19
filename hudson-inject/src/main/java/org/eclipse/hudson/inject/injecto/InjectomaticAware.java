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

package org.eclipse.hudson.inject.injecto;

/**
 * Allows any object to become aware of the {@link Injectomatic} component w/o having to be injectable itself.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public interface InjectomaticAware
{
    /**
     * Notify target of {@link Injectomatic} component.
     */
    void setInjectomatic(Injectomatic injecto);
}
