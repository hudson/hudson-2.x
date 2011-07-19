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

import java.io.PrintWriter;

/**
 * String-based {@link PrintWriter} backed by {@link StringBuilderWriter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PrintBuffer
    extends PrintWriter
{
    public PrintBuffer() {
        super(new StringBuilderWriter(), true);
    }

    public StringBuilder getBuffer() {
        return ((StringBuilderWriter)out).getBuffer();
    }

    public void reset() {
        getBuffer().setLength(0);
    }

    @Override
    public String toString() {
        return getBuffer().toString();
    }
}
