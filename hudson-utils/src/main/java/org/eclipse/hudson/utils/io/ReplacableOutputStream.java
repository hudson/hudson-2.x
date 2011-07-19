/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.utils.io;

import java.io.OutputStream;

/**
 * Allows the target output stream delegate to be changed.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ReplacableOutputStream
    extends DelegatingOutputStream
{
    public ReplacableOutputStream(final OutputStream out) {
        super(out);
    }

    public void set(final OutputStream out) {
        setDelegate(out);
    }
}
