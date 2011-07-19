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

package hudson.model;

import hudson.model.Queue.Task;

/**
 * {@link Item} that can be "built", for
 * whatever meaning of "build".
 *
 * <p>
 * This interface is used by utility code.
 *
 * @author Kohsuke Kawaguchi
 */
public interface BuildableItem extends Item, Task {
	/**
	 * @deprecated
	 *    Use {@link #scheduleBuild(Cause)}.  Since 1.283
	 */
    boolean scheduleBuild();
	boolean scheduleBuild(Cause c);
	/**
	 * @deprecated
	 *    Use {@link #scheduleBuild(int, Cause)}.  Since 1.283
	 */
	boolean scheduleBuild(int quietPeriod);
	boolean scheduleBuild(int quietPeriod, Cause c);
}
