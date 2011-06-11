/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link List} which only allows unique elements; similar to {@link java.util.Set} but typed as {@link List}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("unique-list")
public class UniqueList<E>
    extends AbstractList<E>
{
    /**
     * All list operations delegate to this object.
     */
    @XStreamImplicit
    private final List<E> delegate;

    /**
     * Used to provide object uniqueness.
     */
    @XStreamOmitField
    private /*final*/ Set<E> unique = new LinkedHashSet<E>();

    public UniqueList(final List<E> delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    public UniqueList() {
        this(new ArrayList<E>());
    }

    @SuppressWarnings({"unused"})
    private Object readResolve() {
        if (delegate == null) {
            return null;
        }
        // Rebuild the uniqueness set
        unique = new HashSet<E>();
        Iterator<E> iter = delegate.iterator();
        while (iter.hasNext()) {
            E element = iter.next();
            if (unique.contains(element)) {
                // Serialized state contains duplicate, remove it
                iter.remove();
            }
            else {
                unique.add(element);
            }
        }
        return this;
    }

    @Override
    public boolean contains(final Object obj) {
        return unique.contains(obj);
    }

    @Override
    public E get(final int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public E set(final int index, final E element) {
        if (!contains(element)) {
            unique.add(element);
            return delegate.set(index, element);
        }
        return null;
    }

    @Override
    public void add(final int index, final E element) {
        if (!contains(element)) {
            unique.add(element);
            delegate.add(index, element);
        }
    }

    @Override
    public E remove(final int index) {
        E obj = delegate.remove(index);
        unique.remove(obj);
        return obj;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
