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

import java.io.ObjectStreamException;

/**
 * Used internally in the remoting code to have the proxy object
 * implement readResolve.
 *
 * @author Kohsuke Kawaguchi
 */
public interface IReadResolve {
    Object readResolve() throws ObjectStreamException;
}
