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
*    Kohsuke Kawaguchi, Tom Huybrechts,   Andrew Bayer
 *     
 *
 *******************************************************************************/ 

package hudson.model.listeners;

import hudson.ExtensionPoint;
import hudson.ExtensionListView;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.XmlFile;
import hudson.model.Hudson;
import hudson.model.Saveable;
import hudson.util.CopyOnWriteList;

/**
 * Receives notifications about save actions on {@link Saveable} objects in Hudson.
 *
 * <p>
 * This is an abstract class so that methods added in the future won't break existing listeners.
 *
 * @author Andrew Bayer
 * @since 1.334
 */
public abstract class SaveableListener implements ExtensionPoint {

    /**
     * Called when a change is made to a {@link Saveable} object.
     *
     * @param o
     *      The saveable object.
     * @param file
     *      The {@link XmlFile} for this saveable object.
     */
    public void onChange(Saveable o, XmlFile file) {}

    /**
     * Registers this object as an active listener so that it can start getting
     * callbacks invoked.
     *
     * @deprecated as of 1.281
     *      Put {@link Extension} on your class to get it auto-registered.
     */
    public void register() {
        all().add(this);
    }

    /**
     * Reverse operation of {@link #register()}.
     */
    public void unregister() {
        all().remove(this);
    }

    /**
     * Fires the {@link #onChange} event.
     */
    public static void fireOnChange(Saveable o, XmlFile file) {
        for (SaveableListener l : all()) {
            l.onChange(o,file);
        }
    }

    /**
     * Returns all the registered {@link SaveableListener} descriptors.
     */
    public static ExtensionList<SaveableListener> all() {
        return Hudson.getInstance().getExtensionList(SaveableListener.class);
    }
}
