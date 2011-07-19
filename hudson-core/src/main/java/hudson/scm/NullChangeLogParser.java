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
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * {@link ChangeLogParser} for no SCM.
 * @author Kohsuke Kawaguchi
 */
public class NullChangeLogParser extends ChangeLogParser {
    public ChangeLogSet<? extends ChangeLogSet.Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        return ChangeLogSet.createEmpty(build);
    }
}
