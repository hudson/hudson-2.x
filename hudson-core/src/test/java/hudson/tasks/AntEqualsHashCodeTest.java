/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
package hudson.tasks;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Verify equals and hashCode methods for Ant object.
 * <p/>
 * Date: 10/5/11
 *
 * @author Nikita Levyankov
 */
public class AntEqualsHashCodeTest {
    private Ant ant = new Ant("targets", "antName", "antOpts", "buildFile", "properties");

    @Test
    public void testHashCode() {
        assertEquals(ant.hashCode(), new Ant("targets", "antName", "antOpts", "buildFile", "properties").hashCode());
        assertFalse(ant.hashCode() == new Ant("targets", "antName", "antOpts", "buildFile", "properties1").hashCode());
        assertFalse(ant.hashCode() == new Ant("targets", "antName", "antOpts", "buildFile1", "properties").hashCode());
        assertFalse(ant.hashCode() == new Ant("targets", "antName", "antOpts1", "buildFile", "properties").hashCode());
        assertFalse(ant.hashCode() == new Ant("targets", "antName1", "antOpts", "buildFile", "properties").hashCode());
        assertFalse(ant.hashCode() == new Ant("targets1", "antName", "antOpts", "buildFile", "properties").hashCode());
        assertFalse(
            ant.hashCode() == new Ant("targets1", "antName1", "antOpts1", "buildFile1", "properties1").hashCode());
    }

    @Test
    public void testEqual() {
        assertEquals(ant, ant);
        assertFalse(ant.equals(null));
        assertFalse(ant.equals(new Object()));
        assertEquals(ant, new Ant("targets", "antName", "antOpts", "buildFile", "properties"));
        assertFalse(ant.equals(new Ant("targets", "antName", "antOpts", "buildFile", "properties1")));
        assertFalse(ant.equals(new Ant("targets", "antName", "antOpts", "buildFile1", "properties")));
        assertFalse(ant.equals(new Ant("targets", "antName", "antOpts1", "buildFile", "properties")));
        assertFalse(ant.equals(new Ant("targets", "antName1", "antOpts", "buildFile", "properties")));
        assertFalse(ant.equals(new Ant("targets1", "antName", "antOpts", "buildFile", "properties")));
        assertFalse(ant.equals(new Ant("targets1", "antName1", "antOpts1", "buildFile1", "properties1")));
    }
}
