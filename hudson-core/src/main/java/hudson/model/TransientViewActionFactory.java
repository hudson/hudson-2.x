/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension point for adding transient {@link Action}s to {@link View}s.
 *
 * @since 1.388
 */
public abstract class TransientViewActionFactory implements ExtensionPoint {

	/**
	 * returns a list of (transient) actions never null, may be empty
	 * 
	 * @param v
	 * @return
	 */
	public abstract List<Action> createFor(View v);
	
    /**
     * Returns all the registered {@link TransientViewActionFactory}s.
     */
	public static ExtensionList<TransientViewActionFactory> all() {
		return Hudson.getInstance().getExtensionList(TransientViewActionFactory.class);
	}
	
    /**
     * Creates {@link Action)s for a view, using all registered {@link TransientViewActionFactory}s.
     */
	public static List<Action> createAllFor(View v) {
		List<Action> result = new ArrayList<Action>();
		for (TransientViewActionFactory f: all()) {
			result.addAll(f.createFor(v));
		}
		return result;
	}

}
