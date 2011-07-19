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

package hudson.scm;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.scm.ChangeLogSet.Entry;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * Encapsulates the file format of the changelog.
 *
 * Instances should be stateless, but
 * persisted as a part of {@link Build}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ChangeLogParser {
    public abstract ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException;
}
