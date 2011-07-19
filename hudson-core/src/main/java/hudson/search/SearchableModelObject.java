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

package hudson.search;

import hudson.model.ModelObject;

/**
 * {@link ModelObject} that can be searched.
 *
 * <p>
 * This interface also extends {@link SearchItem} since
 * often {@link ModelObject}s form a natural tree structure,
 * and it's convenient for the model objects themselves to implement
 * the {@link SearchItem} for the edges that form this tree.
 *
 * @author Kohsuke Kawaguchi
 */
public interface SearchableModelObject extends ModelObject, SearchItem {
    /**
     * This binds {@link Search} object to the URL hierarchy.
     */
    Search getSearch();
}
