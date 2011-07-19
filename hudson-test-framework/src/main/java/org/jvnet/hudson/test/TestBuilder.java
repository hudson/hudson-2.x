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

package org.jvnet.hudson.test;

import hudson.tasks.Builder;
import hudson.model.Descriptor;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.Launcher;

import java.io.IOException;

/**
 * Partial {@link Builder} implementation for writing a one-off throw-away builder used during tests.
 *
 * <p>
 * Because this builder tends to be written as an inner class, this builder doesn't write itself
 * to a disk when persisted. Configuration screen won't work, either.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TestBuilder extends Builder {

    public abstract boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException;

    public Descriptor<Builder> getDescriptor() {
        throw new UnsupportedOperationException();
    }

    private Object writeReplace() { return new Object(); }
}
