/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson;

import hudson.model.Hudson;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker interface that designates extensible components
 * in Hudson that can be implemented by plugins.
 *
 * <p>
 * See respective interfaces/classes for more about how to register custom
 * implementations to Hudson. See {@link Extension} for how to have
 * Hudson auto-discover your implementations.
 *
 * <p>
 * This interface is used for auto-generating
 * documentation.
 *
 * @author Kohsuke Kawaguchi
 * @see Plugin
 * @see Extension
 */
public interface ExtensionPoint {
    /**
     * Used by designers of extension points (direct subtypes of {@link ExtensionPoint}) to indicate that
     * the legacy instances are scoped to {@link Hudson} instance. By default, legacy instances are
     * static scope.  
     */
    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface LegacyInstancesAreScopedToHudson {}
}
