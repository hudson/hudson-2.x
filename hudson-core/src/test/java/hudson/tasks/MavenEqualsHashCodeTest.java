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
 * Verify equals and hashCode methods for Maven object.
 * <p/>
 * Date: 10/5/11
 *
 * @author Nikita Levyankov
 */
public class MavenEqualsHashCodeTest {

    private Maven maven = new Maven("targets", "name", "pom", "properties", "jvmOptions", false);

    @Test
    public void testHashCode() {
        assertEquals(maven.hashCode(),
            new Maven("targets", "name", "pom", "properties", "jvmOptions", false).hashCode());
        assertFalse(
            maven.hashCode() == new Maven("targets", "name", "pom", "properties", "jvmOptions", true).hashCode());
        assertFalse(
            maven.hashCode() == new Maven("targets", "name", "pom", "properties", "jvmOptions1", false).hashCode());
        assertFalse(
            maven.hashCode() == new Maven("targets", "name", "pom", "properties1", "jvmOptions", false).hashCode());
        assertFalse(
            maven.hashCode() == new Maven("targets", "name", "pom1", "properties", "jvmOptions", false).hashCode());
        assertFalse(
            maven.hashCode() == new Maven("targets", "name1", "pom", "properties", "jvmOptions", false).hashCode());
        assertFalse(
            maven.hashCode() == new Maven("targets1", "name", "pom", "properties", "jvmOptions", false).hashCode());
    }

    @Test
    public void testEqual() {
        assertEquals(maven, maven);
        assertFalse(maven.equals(null));
        assertFalse(maven.equals(new Object()));
        assertEquals(maven, new Maven("targets", "name", "pom", "properties", "jvmOptions", false));
        assertFalse(maven.equals(new Maven("targets", "name", "pom", "properties", "jvmOptions", true)));
        assertFalse(maven.equals(new Maven("targets", "name", "pom", "properties", "jvmOptions1", false)));
        assertFalse(maven.equals(new Maven("targets", "name", "pom1", "properties", "jvmOptions", false)));
        assertFalse(maven.equals(new Maven("targets", "name1", "pom", "properties", "jvmOptions", false)));
        assertFalse(maven.equals(new Maven("targets1", "name", "pom", "properties", "jvmOptions", false)));
        assertFalse(maven.equals(new Maven("targets1", "name1", "pom1", "properties1", "jvmOptions1", true)));
    }
}
