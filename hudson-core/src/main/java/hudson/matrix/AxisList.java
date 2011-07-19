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
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.matrix;

import com.thoughtworks.xstream.XStream;
import hudson.Util;
import hudson.model.Label;
import hudson.util.RobustCollectionConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Set;

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
