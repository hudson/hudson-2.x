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

import java.util.Iterator;

/**
 * {@link Iterator} that adapts the values returned from another iterator.
 *
 * <p>
 * This class should be really in {@link Iterators} but for historical reasons it's here.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.121
 * @see Iterators
 */
public abstract class AdaptedIterator<T,U> implements Iterator<U> {
    private final Iterator<? extends T> core;

    protected AdaptedIterator(Iterator<? extends T> core) {
        this.core = core;
    }

    protected AdaptedIterator(Iterable<? extends T> core) {
        this(core.iterator());
    }

    public boolean hasNext() {
        return core.hasNext();
    }

    public U next() {
        return adapt(core.next());
    }

    protected abstract U adapt(T item);

    public void remove() {
        core.remove();
    }
}
