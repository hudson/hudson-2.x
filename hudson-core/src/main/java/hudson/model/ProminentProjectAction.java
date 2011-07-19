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

import hudson.tasks.BuildStep;
import hudson.tasks.BuildWrapper;
import hudson.triggers.Trigger;

/**
 * Marker interface for {@link Action}s that should be displayed
 * at the top of the project page.
 *
 * {@link #getIconFileName()}, {@link #getUrlName()}, {@link #getDisplayName()}
 * are used to create a large, more visible icon in the top page to draw
 * users' attention.
 *
 * @see BuildStep#getProjectActions(AbstractProject)
 * @see BuildWrapper#getProjectActions(AbstractProject)
 * @see Trigger#getProjectActions()
 * @see JobProperty#getJobActions(Job)
 *
 * @author Kohsuke Kawaguchi
 */
public interface ProminentProjectAction extends Action {
    // TODO: do the rendering of the part from the action page
}
