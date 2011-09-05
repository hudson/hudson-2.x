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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order-preserving tag cloud (http://en.wikipedia.org/wiki/Tag_cloud)
 * where each keyword gets a weight and displayed according to their weight.
 *
 * TODO: define a view on its own.
 * 
 * @since 1.322
 */
public class TagCloud<T> extends AbstractList<TagCloud<T>.Entry> {
    public final class Entry {
        //TODO: review and check whether we can do it private
        public final T item;
        public final float weight;

        public Entry(T item, float weight) {
            this.item = item;
            this.weight = weight;
        }

        public float scale() {
            // TODO: it's not obvious if linear scaling is the right approach or not.  
            return weight*9/max;
        }

        public String getClassName() {
            return "tag"+((int)scale());
        }

        public T getItem() {
            return item;
        }

        public float getWeight() {
            return weight;
        }
    }

    public interface WeightFunction<T> {
        float weight(T item);
    }

    private final List<Entry> entries = new ArrayList<Entry>();
    private float max = 1;

    /**
     * Creates a tag cloud.
     *
     * @param f
     *      Assigns weight to each item.
     */
    public TagCloud(Iterable<? extends T> inputs, WeightFunction<T> f) {
        for (T input : inputs) {
            float w = Math.abs(f.weight(input));
            max = Math.max(w,max);
            entries.add(new Entry(input, w));
        }
    }

    public Entry get(int index) {
        return entries.get(index);
    }

    public int size() {
        return entries.size();
    }
}
