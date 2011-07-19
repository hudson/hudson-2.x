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

package org.eclipse.hudson.service;

/**
 * Thrown when a system entity that is needed to perform a service level action
 * cannot be found within the system.
 * <p>
 * An example of where this may be useful is when a source project identified by
 * a project name may not exist any more, and therefore cannot be copied to a
 * new project. Also, typically of the service APIs, all {@literal get*} methods
 * may throw this when an entity is not found.
 * <p>
 * Extend this class with a specific implementation for each type of system
 * entity that may not be found.
 * <p>
 * Service API users should do their best to verify an entity exists prior to
 * performing an operation that may trigger this exception.
 *
 * @since 2.1.0
 */
public abstract class NotFoundException extends ServiceRuntimeException {
    protected NotFoundException() {
    }

    protected NotFoundException(String message) {
        super(message);
    }

    protected NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    protected NotFoundException(Throwable cause) {
        super(cause);
    }
}
