/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Anton Kozak
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
package hudson.matrix;

import com.thoughtworks.xstream.XStream;
import hudson.Util;
import hudson.util.RobustCollectionConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;

/**
 * List of {@link Axis}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class AxisList extends ArrayList<Axis> {

    public AxisList() {
    }

    public AxisList(Collection<? extends Axis> c) {
        super(c);
    }

    public AxisList(Axis... c) {
        this(Arrays.asList(c));
    }

    public Axis find(String name) {
        for (Axis a : this) {
            if(a.name.equals(name))
                return a;
        }
        return null;
    }

    /**
     * Creates a subset of the list that only contains the type assignable to the specified type.
     */
    public AxisList subList(Class<? extends Axis> subType) {
        return new AxisList(Util.filter(this,subType));
    }

    @Override
    public boolean add(Axis axis) {
        return axis!=null && super.add(axis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AxisList axisList = (AxisList) o;

        return CollectionUtils.isEqualCollection(this, axisList);
    }

    @Override
    public int hashCode() {
        return ListUtils.hashCodeForList(this);
    }

    /**
     * List up all the possible combinations of this list.
     */
    public Iterable<Combination> list() {
        final int[] base = new int[size()];
        if (base.length==0) return Collections.<Combination>emptyList();

        int b = 1;
        for( int i=size()-1; i>=0; i-- ) {
            base[i] = b;
            b *= get(i).size();
        }

        final int total = b;    // number of total combinations

        return new Iterable<Combination>() {
            public Iterator<Combination> iterator() {
                return new Iterator<Combination>() {
                    private int counter = 0;

                    public boolean hasNext() {
                        return counter<total;
                    }

                    public Combination next() {
                        String[] data = new String[size()];
                        int x = counter++;
                        for( int i=0; i<data.length; i++) {
                            data[i] = get(i).value(x/base[i]);
                            x %= base[i];
                        }
                        assert x==0;
                        return new Combination(AxisList.this,data);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * {@link Converter} implementation for XStream.
     */
    public static final class ConverterImpl extends RobustCollectionConverter {
        public ConverterImpl(XStream xs) {
            super(xs);
        }

        @Override
        public boolean canConvert(Class type) {
            return type==AxisList.class;
        }

        @Override
        protected Object createCollection(Class type) {
            return new AxisList();
        }
    }
}
