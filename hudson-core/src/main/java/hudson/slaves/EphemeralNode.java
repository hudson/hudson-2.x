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

package hudson.slaves;

import hudson.model.Node;

/**
 * {@link Node}s that are not persisted as configuration by itself.
 *
 * @author Kohsuke Kawaguchi
 */
public interface EphemeralNode {
    /**
     * Type-safe cast.
     */
    Node asNode();
}
