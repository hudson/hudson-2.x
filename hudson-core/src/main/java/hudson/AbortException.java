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

import java.io.IOException;

/**
 * Signals a failure where the error was anticipated and diagnosed.
 * When this exception is caught,
 * the stack trace will not be printed, and the build will be marked as a failure.
 *
 * @author Kohsuke Kawaguchi
*/
public final class AbortException extends IOException {
    public AbortException() {
    }

    /**
     * When this exception is caught, the specified message will be reported.
     * @since 1.298
     */
    public AbortException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}
