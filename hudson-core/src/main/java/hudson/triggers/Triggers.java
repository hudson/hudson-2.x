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

package hudson.triggers;

import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.DescriptorList;
import hudson.Extension;

import java.util.List;
import java.util.ArrayList;

/**
 * List of all installed {@link Trigger}s.
 *
 * @author Kohsuke Kawaguchi
 * @deprecated as of 1.286
 *      See each member for how to migrate your code.
 */
public class Triggers {
    /**
     * All registered {@link TriggerDescriptor} implementations.
     * @deprecated as of 1.286
     *      Use {@link Trigger#all()} for read access, and {@link Extension} for registration.
     */
    public static final List<TriggerDescriptor> TRIGGERS = (List)new DescriptorList<Trigger<?>>((Class)Trigger.class);
//    Descriptor.toList(
//        SCMTrigger.DESCRIPTOR,
//        TimerTrigger.DESCRIPTOR
//    );

    /**
     * Returns a subset of {@link TriggerDescriptor}s that applys to the given item.
     *
     * @deprecated as of 1.286
     *      Use {@link Trigger#for_(Item)}.
     */
    public static List<TriggerDescriptor> getApplicableTriggers(Item i) {
        return Trigger.for_(i);
    }
}
