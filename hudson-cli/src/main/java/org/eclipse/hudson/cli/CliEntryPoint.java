/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package org.eclipse.hudson.cli;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Remotable interface for CLI entry point on the server side.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CliEntryPoint {
    /**
     * Just like the static main method.
     *
     * @param locale
     *      Locale of this client.
     */
    int main(List<String> args, Locale locale, InputStream stdin, OutputStream stdout, OutputStream stderr);

    /**
     * Does the named command exist?
     */
    boolean hasCommand(String name);

    /**
     * Returns {@link #VERSION}, so that the client and the server can detect version incompatibility
     * gracefully.
     */
    int protocolVersion();

    int VERSION = 1;
}
