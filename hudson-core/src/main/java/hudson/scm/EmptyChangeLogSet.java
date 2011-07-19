/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Nikita Levyankov
 *      
 *
 *******************************************************************************/ 

package hudson.scm;

import hudson.model.AbstractBuild;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * {@link ChangeLogSet} that's empty.
 *
 * @author Kohsuke Kawaguchi
 * @author Nikita Levyankov
 */
final class EmptyChangeLogSet extends ChangeLogSet<ChangeLogSet.Entry> {
    /*package*/ EmptyChangeLogSet(AbstractBuild<?, ?> build) {
        super(build);
    }

    @Override
    public Collection<Entry> getLogs() {
        return Collections.emptySet();
    }
}
