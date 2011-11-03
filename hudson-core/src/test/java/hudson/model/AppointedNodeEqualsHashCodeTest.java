/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak
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

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static junit.framework.Assert.assertEquals;

/**
 * Test for equals() and hashCode methods of {@link AppointedNode}.
 *
 * Date: 11/2/11
 */
@RunWith(Parameterized.class)
public class AppointedNodeEqualsHashCodeTest {

    private boolean expectedResult;
    private AppointedNode node1;
    private AppointedNode node2;

    public AppointedNodeEqualsHashCodeTest(boolean expectedResult, AppointedNode node1, AppointedNode node2) {
        this.expectedResult = expectedResult;
        this.node1 = node1;
        this.node2 = node2;
    }

    @Parameterized.Parameters
    public static Collection generateData() {
        return Arrays.asList(new Object[][] {
            {true, new AppointedNode("node", true), new AppointedNode("node", true)},
            {false, new AppointedNode("node", true), new AppointedNode("node2", true)},
            {false, new AppointedNode("node", true), new AppointedNode(null, true)},
            {false, new AppointedNode("node", false), new AppointedNode("node", true)},
            {false, new AppointedNode("node", true), new AppointedNode("node", null)},
            {true, new AppointedNode(null, false), new AppointedNode(null, false)},
            {true, new AppointedNode("node", null), new AppointedNode("node", null)}
        });
    }

    @Test
    public void testEquals() {
        assertEquals(expectedResult, node1.equals(node2));
    }

    @Test
    public void testHashCode() {
        assertEquals(expectedResult, node1.hashCode() == node2.hashCode());
    }
}
