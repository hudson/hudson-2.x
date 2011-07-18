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

package hudson.util;

import hudson.FilePath;
import hudson.Util;
import hudson.remoting.Which;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Used to build up an argument in the classpath format.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.300
 */
public class ClasspathBuilder implements Serializable, Iterable<String> {
    private final List<String> args = new ArrayList<String>();

    /**
     * Adds a single directory or a jar file.
     */
    public ClasspathBuilder add(File f) {
        return add(f.getAbsolutePath());
    }

    /**
     * Adds a single directory or a jar file.
     */
    public ClasspathBuilder add(FilePath f) {
        return add(f.getRemote());
    }

    /**
     * Adds a single directory or a jar file.
     */
    public ClasspathBuilder add(String path) {
        args.add(path);
        return this;
    }

    /**
     * Adds a jar file that contains the given class.
     * @since 1.361
     */
    public ClasspathBuilder addJarOf(Class c) throws IOException {
        return add(Which.jarFile(c));
    }

    /**
     * Adds all the files that matches the given glob in the directory.
     *
     * @see FilePath#list(String)  
     */
    public ClasspathBuilder addAll(FilePath base, String glob) throws IOException, InterruptedException {
        for(FilePath item : base.list(glob))
            add(item);
        return this;
    }

    /**
     * @since 2.1.0
     */
    public Iterator<String> iterator() {
        return args.iterator();
    }

    /**
     * Returns the string representation of the classpath.
     */
    @Override
    public String toString() {
        return toString(File.pathSeparator);
    }

    /**
     * @since 2.1.0
     */
    public String toString(final String sep) {
        return Util.join(args,sep);
    }
}
