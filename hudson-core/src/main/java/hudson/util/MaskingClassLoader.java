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

package hudson.util;

import java.util.Collection;

/**
 * {@link ClassLoader} that masks a specified set of classes
 * from its parent class loader.
 *
 * <p>
 * This code is used to create an isolated environment.
 *
 * @author Kohsuke Kawaguchi
 */
public class MaskingClassLoader extends ClassLoader {
    /**
     * Prefix of the packages that should be hidden.
     */
    private final String[] masks;

    public MaskingClassLoader(ClassLoader parent, String... masks) {
        super(parent);
        this.masks = masks;
    }

    public MaskingClassLoader(ClassLoader parent, Collection<String> masks) {
        this(parent, masks.toArray(new String[masks.size()]));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String mask : masks) {
            if(name.startsWith(mask))
                throw new ClassNotFoundException();
        }

        return super.loadClass(name, resolve);
    }
}
