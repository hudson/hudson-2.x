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

package hudson.lifecycle;

/**
 * Indicates that the {@link Lifecycle} doesn't support restart.
 * 
 * @author Kohsuke Kawaguchi
 */
public class RestartNotSupportedException extends Exception {
    public RestartNotSupportedException(String reason) {
        super(reason);
    }

    public RestartNotSupportedException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
