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

import junit.framework.TestCase;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConsistentHashTest extends TestCase {
    /**
     * Just some random tests to ensure that we have no silly NPE or that kind of error.
     */
    public void testBasic() {
        ConsistentHash<String> hash = new ConsistentHash<String>();
        hash.add("data1");
        hash.add("data2");
        hash.add("data3");

        System.out.println(hash.lookup(0));

        // there's one in 2^32 chance that this test fails, but these two query points are
        // only off by one.
        String x = hash.lookup(Integer.MIN_VALUE);
        String y = hash.lookup(Integer.MAX_VALUE);
        assertEquals(x,y);

        // list them up
        Iterator<String> itr = hash.list(Integer.MIN_VALUE).iterator();
        Set<String> all = new HashSet<String>();
        String z = itr.next();
        all.add(z);
        assertEquals(z,x);
        all.add(itr.next());
        all.add(itr.next());
        assertTrue(!itr.hasNext());
        assertEquals(3,all.size());
    }

    /**
     * Uneven distribution should result in uneven mapping.
     */
    public void testUnevenDisribution() {
        ConsistentHash<String> hash = new ConsistentHash<String>();
        hash.add("even",10);
        hash.add("odd",100);

        Random r = new Random(0);
        int even=0,odd=0;
        for(int i=0; i<1000; i++) {
            String v = hash.lookup(r.nextInt());
            if(v.equals("even"))    even++;
            else                    odd++;
        }

        // again, there's a small chance tha this test fails. 
        System.out.printf("%d/%d\n",even,odd);
        assertTrue(even*8<odd);
    }

    /**
     * Removal shouldn't affect existing nodes
     */
    public void testRemoval() {
        ConsistentHash<Integer> hash = new ConsistentHash<Integer>();
        for( int i=0; i<10; i++ )
            hash.add(i);

        // what was the mapping before the mutation?
        Map<Integer,Integer> before = new HashMap<Integer, Integer>();
        Random r = new Random(0);
        for(int i=0; i<1000; i++) {
            int q = r.nextInt();
            before.put(q,hash.lookup(q));
        }

        // remove a node
        hash.remove(0);

        // verify that the mapping remains consistent
        for (Entry<Integer,Integer> e : before.entrySet()) {
            int m = hash.lookup(e.getKey());
            assertTrue(e.getValue()==0 || e.getValue()==m);
        }
    }

    public void testEmptyBehavior() {
        ConsistentHash<String> hash = new ConsistentHash<String>();
        assertFalse(hash.list(0).iterator().hasNext());
        assertNull(hash.lookup(0));
        assertNull(hash.lookup(999));
    }
}
