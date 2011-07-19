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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages unique ID for classloaders.
 *
 * @author Kohsuke Kawaguchi
 */
final class ExportedClassLoaderTable {
    private final Map<Integer, WeakReference<ClassLoader>> table = new HashMap<Integer, WeakReference<ClassLoader>>();
    private final WeakHashMap<ClassLoader,Integer> reverse = new WeakHashMap<ClassLoader,Integer>();

    // id==0 is reserved for bootstrap classloader
    private int iota = 1;


    public synchronized int intern(ClassLoader cl) {
        if(cl==null)    return 0;   // bootstrap classloader

        Integer id = reverse.get(cl);
        if(id==null) {
            id = iota++;
            table.put(id,new WeakReference<ClassLoader>(cl));
            reverse.put(cl,id);
        }

        return id;
    }

    public synchronized ClassLoader get(int id) {
        WeakReference<ClassLoader> ref = table.get(id);
        if(ref==null)   return null;
        return ref.get();
    }
}
