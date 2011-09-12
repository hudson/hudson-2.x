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

import org.apache.commons.digester3.Digester;

/**
 * {@link Digester} wrapper to fix the issue DIGESTER-118.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.125
 * @deprecated use {@link Digester} instead
 * @since 2.1.2
 */
public class Digester2 extends Digester {
}
