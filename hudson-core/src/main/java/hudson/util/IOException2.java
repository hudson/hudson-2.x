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

import java.io.IOException;

/**
 * {@link IOException} with linked exception.
 *
 * @author Kohsuke Kawaguchi
 */
public class IOException2 extends IOException  {
    private final Throwable cause;

    public IOException2(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    public IOException2(String s, Throwable cause) {
        super(s);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}
