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

/**
 * {@link Action} that puts a little icon (or icons)
 * next to the build in the build history.
 *
 * <p>
 * This can be implemented by {@link Action}s that associate themselves
 * with {@link Run}. 
 *
 * <p>
 * Actions with this marker should have a view <tt>badge.jelly</tt>,
 * which will be called to render the badges. The expected visual appearance
 * of a badge is a 16x16 icon.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.150
 */
public interface BuildBadgeAction extends Action {
}
