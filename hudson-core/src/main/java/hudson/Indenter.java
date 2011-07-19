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

package hudson;

import hudson.model.Job;

/**
 * Used by <tt>projectView.jelly</tt> to indent modules.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Indenter<J extends Job> {
    protected abstract int getNestLevel(J job);

    public final String getCss(J job) {
        return "padding-left: "+getNestLevel(job)*2+"em";
    }

    public final String getRelativeShift(J job) {
        int i = getNestLevel(job);
        if(i==0)    return null;
        return "position:relative; left: "+ i *2+"em";
    }
}
