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
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.EnvVars;
import hudson.slaves.NodeSpecific;

/**
 * Represents any concept that can be adapted for a certain environment.
 * 
 * Mainly for documentation purposes.
 *
 * @since 1.286
 * @param <T>
 *      Concrete type that represents the thing that can be adapted.
 * @see NodeSpecific
 */
public interface EnvironmentSpecific<T extends EnvironmentSpecific<T>> {
	/**
	 * Returns a specialized copy of T for functioning in the given environment.
	 */
	T forEnvironment(EnvVars environment);
}
