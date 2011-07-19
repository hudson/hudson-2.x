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

import hudson.ExtensionPoint;
import hudson.Extension;

/**
 * Marker interface for actions that are added to {@link Hudson}.
 *
 * <p>
 * Extend from this interface and put {@link Extension} on your subtype
 * to have them auto-registered to {@link Hudson}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.311
 */
public interface RootAction extends Action, ExtensionPoint {
}
