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

package hudson.remoting;

/**
 * {@link Callable} that nominates another claassloader for serialization.
 *
 * <p>
 * For various reasons, one {@link Callable} object (and all the objects reachable from it) is
 * serialized by one classloader.
 * By default, the classloader that loaded {@link Callable} object itself is used,
 * but when {@link Callable} object refers to other objects that are loaded by other classloaders,
 * this will fail to deserialize on the remote end.
 *
 * <p>
 * In such a case, implement this interface, instead of plain {@link Callable} and
 * return a classloader that can see all the classes.
 *
 * In case of Hudson, {@code PluginManager.uberClassLoader} is a good candidate.  
 *
 * @author Kohsuke Kawaguchi
 */
public interface DelegatingCallable<V,T extends Throwable> extends Callable<V,T> {
    ClassLoader getClassLoader();
}
