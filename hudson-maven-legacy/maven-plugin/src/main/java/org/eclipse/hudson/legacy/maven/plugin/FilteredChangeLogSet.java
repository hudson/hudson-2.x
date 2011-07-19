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

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;

/**
 * {@link ChangeLogSet} implementation used for {@link MavenBuild}.
 *
 * @author Kohsuke Kawaguchi
 */
public class FilteredChangeLogSet extends ChangeLogSet<Entry> {
    private final List<Entry> master;

    public final ChangeLogSet<? extends Entry> core;

    /*package*/ FilteredChangeLogSet(MavenBuild build) {
        super(build);
        MavenModuleSetBuild parentBuild = build.getParentBuild();
        if(parentBuild==null) {
            core = ChangeLogSet.createEmpty(build);
            master = Collections.emptyList();
        } else {
            core = parentBuild.getChangeSet();
            master = parentBuild.getChangeSetFor(build.getParent());
        }
    }

    public List<Entry> getLogs() {
        return master;
    }
}
