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
 *    Kohsuke Kawaguchi, Stephen Connolly
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.labels.LabelAtom;

import java.util.Collection;

/**
 * Automatically adds labels to {@link Node}s.
 *
 * <p>
 * To register your implementation, put {@link Extension} on your derived types.
 *
 * @author Stephen Connolly
 * @since 1.323
 *      Signature of this class changed in 1.323, after making sure that no
 *      plugin in the Subversion repository is using this.
 */
public abstract class LabelFinder implements ExtensionPoint {
    /**
     * Returns all the registered {@link LabelFinder}s.
     */
    public static ExtensionList<LabelFinder> all() {
        return Hudson.getInstance().getExtensionList(LabelFinder.class);
    }

    /**
     * Find the labels that the node supports.
     *
     * @param node
     *      The node that receives labels. Never null.
     * @return
     *      A set of labels for the node. Can be empty but never null.
     */
    public abstract Collection<LabelAtom> findLabels(Node node);
}
