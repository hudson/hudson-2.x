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
 * {@link Recorder} is a kind of {@link Publisher} that collects statistics from the build,
 * and can mark builds as unstable/failure. This marking ensures that builds are marked accordingly
 * before notifications are sent via {@link Notifier}s. Otherwise, if the build is marked failed
 * after some notifications are sent, inconsistency ensues.
 *
 * <p>
 * To register a custom {@link Publisher} from a plugin,
 * put {@link Extension} on your descriptor.
 *
 *
 * @author Kohsuke Kawaguchi
 * @since 1.286
 * @see Notifier
 */
public abstract class Recorder extends Publisher implements ExtensionPoint {
    @SuppressWarnings("deprecation") // super only @Deprecated to discourage other subclasses
    protected Recorder() {}
    public BuildStepDescriptor getDescriptor() {
        return (BuildStepDescriptor)super.getDescriptor();
    }
}
