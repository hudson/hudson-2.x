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

package hudson.remoting;

import java.io.Serializable;

/**
 * Represents computation to be done on a remote system.
 *
 * @see Channel
 * @author Kohsuke Kawaguchi
 */
public interface Callable<V,T extends Throwable> extends Serializable {
    /**
     * Performs computation and returns the result,
     * or throws some exception.
     */
    V call() throws T;
}
