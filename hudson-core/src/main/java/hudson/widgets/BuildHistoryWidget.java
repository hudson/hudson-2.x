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

package hudson.widgets;

import hudson.model.Hudson;
import hudson.model.Queue.Item;
import hudson.model.Queue.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays the build history on the side panel.
 *
 * <p>
 * This widget enhances {@link HistoryWidget} by groking the notion
 * that {@link #owner} can be in the queue toward the next build.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildHistoryWidget<T> extends HistoryWidget<Task,T> {
    /**
     * @param owner
     *      The parent model object that owns this widget.
     */
    public BuildHistoryWidget(Task owner, Iterable<T> baseList,Adapter<? super T> adapter) {
        super(owner,baseList, adapter);
    }

    /**
     * Returns the first queue item if the owner is scheduled for execution in the queue.
     */
    public Item getQueuedItem() {
        return Hudson.getInstance().getQueue().getItem(owner);
    }

    /**
     * Returns the queue item if the owner is scheduled for execution in the queue, in REVERSE ORDER
     */
    public List<Item> getQueuedItems() {
    	List<Item> list = new ArrayList<Item>(Hudson.getInstance().getQueue().getItems(owner));
    	Collections.reverse(list);
    	return list;
    }
}
