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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements memoization semantics.
 *
 * <p>
 * Conceputually a function from K -> V that computes values lazily and remembers the results.
 * Often used to implement a data store per key.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.281
 */
public abstract class Memoizer<K,V> {
    private final ConcurrentHashMap<K,V> store = new ConcurrentHashMap<K,V>();

    public V get(K key) {
        V v = store.get(key);
        if(v!=null)     return v;

        // TODO: if we want to, we can avoid locking altogether by putting a sentinel value
        // that represents "the value is being computed". FingerprintMap does this.
        synchronized (this) {
            v = store.get(key);
            if(v!=null)     return v;

            v = compute(key);
            store.put(key,v);
            return v;
        }
    }

    /**
     * Creates a new instance.
     */
    public abstract V compute(K key);

    /**
     * Clears all the computed values.
     */
    public void clear() {
        store.clear();
    }

    /**
     * Provides a snapshot view of all {@code V}s.
     */
    public Iterable<V> values() {
        return store.values();
    }
}
