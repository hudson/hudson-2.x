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
 * Equals and hashCode test for {@link ChoiceParameterDefinition} object
 * <p/>
 * Date: 11/2/11
 *
 * @author Nikita Levyankov
 */
public class ChoiceParameterDefinitionEqualsHashCodeTest {

    @Test
    public void testEquals() {
        ChoiceParameterDefinition o1 = new ChoiceParameterDefinition("name1", "value\nvalue2", "description");
        assertEquals(o1, o1);
        assertFalse(o1.equals(null));
        assertFalse(o1.equals(new StringParameterDefinition("test1", "value\nvalue2", "description")));
        assertFalse(new ChoiceParameterDefinition("name1", "value\nvalue2", null).equals(
            new ChoiceParameterDefinition(null, "value\nvalue2", null)));
        assertFalse(new ChoiceParameterDefinition(null, "value1", null).equals(
            new ChoiceParameterDefinition(null, "value\nvalue2", null)));
        assertFalse(o1.equals(new ChoiceParameterDefinition(null, "value1", null)));
        assertFalse(o1.equals(new ChoiceParameterDefinition("name1", "value1", "description")));

        assertEquals(o1, new ChoiceParameterDefinition("name1", "value\nvalue2", "description"));
        assertEquals(o1, new ChoiceParameterDefinition("name1", "value\nvalue2", "description1"));
        assertEquals(new ChoiceParameterDefinition(null, "value\nvalue2", "d1"),
            new ChoiceParameterDefinition(null, "value\nvalue2", "d1"));
        assertEquals(new ChoiceParameterDefinition(null, "value\nvalue2", null),
            new ChoiceParameterDefinition(null, "value\nvalue2", null));
    }

    @Test
    public void testHashCode() {
        ChoiceParameterDefinition o1 = new ChoiceParameterDefinition("name1", "value\nvalue2", "description");
        assertTrue(o1.hashCode() == o1.hashCode());
        assertFalse(o1.hashCode() == new StringParameterDefinition("test1", "value\nvalue2", "description").hashCode());
        assertFalse(new ChoiceParameterDefinition("name1", "value\nvalue2", null).hashCode() ==
            new ChoiceParameterDefinition(null, "value\nvalue2", null).hashCode());
        assertFalse(new ChoiceParameterDefinition(null, "value1", null).hashCode() ==
            new ChoiceParameterDefinition(null, "value\nvalue2", null).hashCode());
        assertFalse(o1.hashCode() == new ChoiceParameterDefinition(null, "value1", null).hashCode());
        assertFalse(o1.hashCode() == new ChoiceParameterDefinition("name1", "value1", "description").hashCode());

        assertTrue(o1.hashCode() == new ChoiceParameterDefinition("name1", "value\nvalue2", "description").hashCode());
        assertTrue(o1.hashCode() == new ChoiceParameterDefinition("name1", "value\nvalue2", "description1").hashCode());
        assertTrue(new ChoiceParameterDefinition(null, "value\nvalue2", "d1").hashCode() ==
            new ChoiceParameterDefinition(null, "value\nvalue2", "d1").hashCode());
        assertTrue(new ChoiceParameterDefinition(null, "value\nvalue2", null).hashCode() ==
            new ChoiceParameterDefinition(null, "value\nvalue2", null).hashCode());
    }
}
