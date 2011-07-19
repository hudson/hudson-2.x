/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Nikita Levyankov
 *     
 *
 *******************************************************************************/ 

package org.jvnet.hudson.test;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;

import java.util.Collection;
import java.util.List;
import java.util.Collections;


/**
 * @author Andrew Bayer
 */
public class ExtractChangeLogSet extends ChangeLogSet<ExtractChangeLogParser.ExtractChangeLogEntry> {
    private List<ExtractChangeLogParser.ExtractChangeLogEntry> changeLogs = null;

    public ExtractChangeLogSet(AbstractBuild<?, ?> build, List<ExtractChangeLogParser.ExtractChangeLogEntry> changeLogs) {
        super(build);
        for (ExtractChangeLogParser.ExtractChangeLogEntry entry : changeLogs) {
            entry.setParent(this);
        }
        this.changeLogs = Collections.unmodifiableList(changeLogs);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<ExtractChangeLogParser.ExtractChangeLogEntry> getLogs() {
        return changeLogs;
    }
}
