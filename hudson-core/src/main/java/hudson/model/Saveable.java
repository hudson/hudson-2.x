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

import hudson.BulkChange;
import hudson.model.listeners.SaveableListener;
import java.io.IOException;

/**
 * Object whose state is persisted to XML.
 *
 * @author Kohsuke Kawaguchi
 * @see BulkChange
 * @since 1.249
 */
public interface Saveable {
    /**
     * Persists the state of this object into XML.
     *
     * <p>
     * For making a bulk change efficiently, see {@link BulkChange}.
     *
     * <p>
     * To support listeners monitoring changes to this object, call {@link SaveableListener.fireOnChange}
     * @throws IOException
     *      if the persistence failed.
     */
    void save() throws IOException;

    /**
     * {@link Saveable} that doesn't save anything.
     * @since 1.301.
     */
    Saveable NOOP = new Saveable() {
        public void save() throws IOException {
        }
    };
}
