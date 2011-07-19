/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model;

/**
 * Optional interface for {@link Action}s that add themselves to {@link Run}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface RunAction extends Action {
    /**
     * Called after the build is loaded and the object is added to the build list.
     */
    void onLoad();

    /**
     * Called when the action is aded to the {@link Run} object.
     * @since 1.376
     */
    void onAttached(Run r);

    /**
     * Called after the build is finished.
     */
    void onBuildComplete();
}
