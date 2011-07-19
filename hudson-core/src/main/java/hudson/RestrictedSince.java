/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       Kohsuke Kawaguchi
 *
 *******************************************************************************/ 

package hudson;

//import org.kohsuke.accmod.Restricted;

import java.lang.annotation.Documented;

/**
 * Accompanies {@link Restricted} annotation to indicate when the access restriction was placed.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.355
 */
@Documented
public @interface RestrictedSince {
    /**
     * Hudson version number that this restriction has started.
     */
    String value();
}
