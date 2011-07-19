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

package hudson.model.queue;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.lang.Math.*;

/**
* Represents a mutable q(t), a discrete value that changes over the time.
*
* <p>
* Internally represented by a set of ranges and the value of q(t) in that range,
* as a map from "starting time of a range" to "value of q(t)".
*/
final class Timeline {
    // int[] is always length=1
    private final TreeMap<Long, int[]> data = new TreeMap<Long, int[]>();

    /**
     * Obtains q(t) for the given t.
     */
    private int at(long t) {
        SortedMap<Long, int[]> head = data.subMap(t,Long.MAX_VALUE);
        if (head.isEmpty()) return 0;
        return data.get(head.firstKey())[0];
    }

    /**
     * Finds smallest t' > t where q(t')!=q(t)
     *
     * If there's no such t' this method returns null.
     */
    private Long next(long t) {
        SortedMap<Long, int[]> x = data.tailMap(t + 1);
        return x.isEmpty() ? null : x.firstKey();
    }

    /**
     * Splits the range set at the given timestamp (if it hasn't been split yet)
     */
    private void splitAt(long t) {
        if (data.containsKey(t)) return; // already split at this timestamp

        SortedMap<Long, int[]> head = data.headMap(t);

        int v = head.isEmpty() ? 0 : data.get(head.lastKey())[0];
        data.put(t, new int[]{v});
    }

    /**
     * increases q(t) by n for t in [start,end).
     *
     * @return peak value of q(t) in this range as a result of addition.
     */
    int insert(long start, long end, int n) {
        splitAt(start);
        splitAt(end);

        int peak = 0;
        for (Map.Entry<Long, int[]> e : data.tailMap(start).headMap(end).entrySet()) {
            peak = max(peak, e.getValue()[0] += n);
        }
        return peak;
    }

    /**
     * Finds a "valley" in this timeline that fits the given duration.
     * <p>
     * More formally, find smallest x that:
     * <ul>
     * <li>x >= start
     * <li>q(t) <= n for all t \in [x,x+duration)
     * </ul>
     *
     * @return null
     *      if no such x was found.
     */
    Long fit(long start, long duration, int n) {
        OUTER:
        while (true) {
            long t = start;
            // check if 'start' satisfies the two conditions by moving t across [start,start+duration)
            while ((t-start)<duration) {
                if (at(t)>n) {
                    // value too big. what's the next t that's worth trying?
                    Long nxt = next(t);
                    if (nxt==null)  return null;
                    start = nxt;
                    continue OUTER;
                } else {
                    Long nxt = next(t);
                    if (nxt==null) t = Long.MAX_VALUE;
                    else           t = nxt;
                }
            }
            // q(t) looks good at the entire [start,start+duration)
            return start;
        }
    }
}
