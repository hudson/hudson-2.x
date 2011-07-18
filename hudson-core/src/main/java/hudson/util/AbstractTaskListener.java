/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import hudson.console.HyperlinkNote;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * Partial default implementation of {@link TaskListener}
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractTaskListener implements TaskListener {
    public void hyperlink(String url, String text) throws IOException {
        annotate(new HyperlinkNote(url,text.length()));
        getLogger().print(text);
    }
}
