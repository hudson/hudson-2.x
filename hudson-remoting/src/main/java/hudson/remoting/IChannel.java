/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.remoting;

/**
 * Internally used to mark methods on {@link Channel} that are exported to remote.
 *
 * <p>
 * Behaviors of the methods are explained in {@link Channel}.
 *
 * @author Kohsuke Kawaguchi
 */
interface IChannel {
    Object getProperty(Object key);
    Object waitForProperty(Object key) throws InterruptedException;
}
