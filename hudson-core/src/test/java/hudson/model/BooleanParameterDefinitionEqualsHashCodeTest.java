/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Nikita Levyankov
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
package hudson.model;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Equals and hashCode test for {@link BooleanParameterDefinition} object
 * <p/>
 * Date: 11/2/11
 *
 * @author Nikita Levyankov
 */
public class BooleanParameterDefinitionEqualsHashCodeTest {

    @Test
    public void testEquals() {
        BooleanParameterDefinition o1 = new BooleanParameterDefinition("name1", false, "description");
        assertEquals(o1, o1);
        assertFalse(o1.equals(null));
        assertFalse(o1.equals(new StringParameterDefinition("test1", "value", "description")));
        assertFalse(new BooleanParameterDefinition("name1", false, null).equals(
            new BooleanParameterDefinition(null, false, null)));
        assertFalse(
            new BooleanParameterDefinition(null, true, null).equals(new BooleanParameterDefinition(null, false, null)));
        assertFalse(o1.equals(new BooleanParameterDefinition(null, true, null)));
        assertFalse(o1.equals(new BooleanParameterDefinition("name1", true, "description")));

        assertEquals(o1, new BooleanParameterDefinition("name1", false, "description"));
        assertEquals(o1, new BooleanParameterDefinition("name1", false, "description1"));
        assertEquals(new BooleanParameterDefinition(null, false, "d1"),
            new BooleanParameterDefinition(null, false, "d1"));
        assertEquals(new BooleanParameterDefinition(null, false, null),
            new BooleanParameterDefinition(null, false, null));
    }

    @Test
    public void testHashCode() {
        BooleanParameterDefinition o1 = new BooleanParameterDefinition("name1", false, "description");
        assertEquals(o1, o1);
        assertFalse(o1.hashCode() == new StringParameterDefinition("test1", "value", "description").hashCode());
        assertFalse(new BooleanParameterDefinition("name1", false, null).hashCode() ==
            new BooleanParameterDefinition(null, false, null).hashCode());
        assertFalse(new BooleanParameterDefinition(null, true, null).hashCode() ==
            new BooleanParameterDefinition(null, false, null).hashCode());
        assertFalse(o1.hashCode() == new BooleanParameterDefinition(null, true, null).hashCode());
        assertFalse(o1.hashCode() == new BooleanParameterDefinition("name1", true, "description").hashCode());

        assertTrue(o1.hashCode() == new BooleanParameterDefinition("name1", false, "description").hashCode());
        assertTrue(o1.hashCode() == new BooleanParameterDefinition("name1", false, "description1").hashCode());
        assertTrue(new BooleanParameterDefinition(null, false, "d1").hashCode() ==
            new BooleanParameterDefinition(null, false, "d1").hashCode());
        assertTrue(new BooleanParameterDefinition(null, false, null).hashCode() ==
            new BooleanParameterDefinition(null, false, null).hashCode());
    }
}
