/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.tasks;

import hudson.Extension;
import hudson.ExtensionPoint;

/**
 * {@link BuildStep}s that run after the build is completed.
 *
 * <p>
 * {@link Notifier} is a kind of {@link Publisher} that sends out the outcome of the builds to
 * other systems and humans. This marking ensures that notifiers are run after the build result
 * is set to its final value by other {@link Recorder}s.  To run even after the build is marked
 * as complete, override {@link #needsToRunAfterFinalized} to return true.
 *
 * <p>
 * To register a custom {@link Publisher} from a plugin,
 * put {@link Extension} on your descriptor.
 *
 *
 * @author Kohsuke Kawaguchi
 * @since 1.286
 * @see Recorder
 */
public abstract class Notifier extends Publisher implements ExtensionPoint {
    @SuppressWarnings("deprecation") // super only @Deprecated to discourage other subclasses
    protected Notifier() {}
    public BuildStepDescriptor getDescriptor() {
        return (BuildStepDescriptor)super.getDescriptor();
    }
}
