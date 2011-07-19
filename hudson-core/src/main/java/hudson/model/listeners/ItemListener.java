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

package hudson.model.listeners;

import hudson.ExtensionPoint;
import hudson.ExtensionList;
import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Item;

/**
 * Receives notifications about CRUD operations of {@link Item}.
 *
 * @since 1.74
 * @author Kohsuke Kawaguchi
 */
public class ItemListener implements ExtensionPoint {
    /**
     * Called after a new job is created and added to {@link Hudson},
     * before the initial configuration page is provided.
     * <p>
     * This is useful for changing the default initial configuration of newly created jobs.
     * For example, you can enable/add builders, etc.
     */
    public void onCreated(Item item) {
    }

    /**
     * Called after a new job is created by copying from an existing job.
     *
     * For backward compatibility, the default implementation of this method calls {@link #onCreated(Item)}.
     * If you choose to handle this method, think about whether you want to call super.onCopied or not.
     *
     *
     * @param src
     *      The source item that the new one was copied from. Never null.
     * @param  item
     *      The newly created item. Never null.
     *
     * @since 1.325
     *      Before this version, a copy triggered {@link #onCreated(Item)}.
     */
    public void onCopied(Item src, Item item) {
        onCreated(item);
    }

    /**
     * Called after all the jobs are loaded from disk into {@link Hudson}
     * object.
     */
    public void onLoaded() {
    }

    /**
     * Called right before a job is going to be deleted.
     *
     * At this point the data files of the job is already gone.
     */
    public void onDeleted(Item item) {
    }

    /**
     * Called after a job is renamed.
     *
     * @param item
     *      The job being renamed.
     * @param oldName
     *      The old name of the job.
     * @param newName
     *      The new name of the job. Same as {@link Item#getName()}.
     * @since 1.146
     */
    public void onRenamed(Item item, String oldName, String newName) {
    }

    /**
     * Registers this instance to Hudson and start getting notifications.
     *
     * @deprecated as of 1.286
     *      put {@link Extension} on your class to have it auto-registered.
     */
    public void register() {
        all().add(this);
    }

    /**
     * All the registered {@link ItemListener}s.
     */
    public static ExtensionList<ItemListener> all() {
        return Hudson.getInstance().getExtensionList(ItemListener.class);
    }

    public static void fireOnCopied(Item src, Item result) {
        for (ItemListener l : all())
            l.onCopied(src,result);
    }

    public static void fireOnCreated(Item item) {
        for (ItemListener l : all())
            l.onCreated(item);
    }
}
