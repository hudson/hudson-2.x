/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.util.io;

import hudson.util.FileVisitor;

import java.io.Closeable;

/**
 * {@link FileVisitor} that creates archive files.
 *
 * @since 1.359
 * @see ArchiverFactory
 */
public abstract class Archiver extends FileVisitor implements Closeable {
    protected int entriesWritten =0;

    /**
     * Number of files/directories archived.
     */
    public int countEntries() {
        return entriesWritten;
    }
}
