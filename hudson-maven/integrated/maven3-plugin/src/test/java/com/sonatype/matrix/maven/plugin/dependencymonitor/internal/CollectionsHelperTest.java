/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.sonatype.matrix.maven.plugin.dependencymonitor.internal.CollectionsHelper.containsAny;
import static com.sonatype.matrix.maven.plugin.dependencymonitor.internal.CollectionsHelper.differs;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CollectionsHelper}.
 */
public class CollectionsHelperTest
{
    @Test
    public void testContainsAny() {
        List<String> bag1 = Arrays.asList("1", "2", "3");
        List<String> bag2 = Arrays.asList("2", "3", "4");

        assertTrue(containsAny(bag1, bag2));

        bag1 = Arrays.asList("5", "6", "7");
        bag2 = Arrays.asList("2", "3", "4");

        assertFalse(containsAny(bag1, bag2));
    }

    @Test
    public void testDiffers() {
        Multimap<String,String> map = HashMultimap.create();

        List<String> deps1 = Arrays.asList("1", "2", "3");
        Collection<String> result1 = map.replaceValues("a", deps1);
        System.out.println(map);
        System.out.println(result1);
        assertTrue(differs(deps1, result1));

        List<String> deps2 = Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9");
        Collection<String> result2 = map.replaceValues("a", deps2);
        System.out.println(map);
        System.out.println(result2);
        assertTrue(differs(deps2, result2));

        List<String> deps3 = Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9");
        Collection<String> result3 = map.replaceValues("a", deps3);
        System.out.println(map);
        System.out.println(result3);
        assertFalse(differs(deps3, result3));

        List<String> deps4 = Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9");
        Collections.shuffle(deps4);
        System.out.println(deps4);

        Collection<String> result4 = map.replaceValues("a", deps4);
        System.out.println(map);
        System.out.println(result4);
        assertFalse(differs(deps4, result4));
    }
}
