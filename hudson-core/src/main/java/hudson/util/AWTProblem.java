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

package hudson.util;

/**
 * @author Kohsuke Kawaguchi
 */
public class AWTProblem extends ErrorObject {
    //TODO: review and check whether we can do it private
    public final Throwable cause;

    public Throwable getCause() {
        return cause;
    }

    public AWTProblem(Throwable cause) {
        this.cause = cause;
    }
}

