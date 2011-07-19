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

package hudson.model;

import java.io.File;

/**
 * Root object of a persisted object tree
 * that gets its own file system directory.
 *
 * @author Kohsuke Kawaguchi
 */
public interface PersistenceRoot extends Saveable {
    /**
     * Gets the root directory on the file system that this
     * {@link Item} can use freely for storing the configuration data.
     *
     * <p>
     * This parameter is given by the {@link ItemGroup} when
     * {@link Item} is loaded from memory.
     */
    File getRootDir();
}
