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

package org.hudsonci.maven.plugin.dependencymonitor.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.hudsonci.maven.plugin.dependencymonitor.internal.CollectionsHelper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hudsonci.maven.plugin.dependencymonitor.internal.CollectionsHelper.containsAny;
import static org.hudsonci.maven.plugin.dependencymonitor.internal.CollectionsHelper.differs;
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
