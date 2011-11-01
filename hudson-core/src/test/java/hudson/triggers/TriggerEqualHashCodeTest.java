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
package hudson.triggers;

import antlr.ANTLRException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Test for equals and hashCode methods of {@link Trigger} objects
 * </p>
 * Date: 10/28/2011
 *
 * @author
 */
@RunWith(Parameterized.class)
public class TriggerEqualHashCodeTest {

    private boolean expectedResult;
    private Trigger trigger1;
    private Trigger trigger2;

    public TriggerEqualHashCodeTest(boolean expectedResult, Trigger trigger1, Trigger trigger2) {
        this.expectedResult = expectedResult;
        this.trigger1 = trigger1;
        this.trigger2 = trigger2;
    }

    @Parameterized.Parameters
    public static Collection generateData() throws ANTLRException {
        return Arrays.asList(new Object[][] {
            {true, new TimerTrigger(""), new TimerTrigger("")},
            {true, new TimerTrigger("* * * * *"), new TimerTrigger("* * * * *")},
            {false, new TimerTrigger("* * * * *"), new TimerTrigger("*/2 * * * *")},
            {true, new SCMTrigger(""), new SCMTrigger("")},
            {true, new SCMTrigger("* * * * *"), new SCMTrigger("* * * * *")},
            {false, new SCMTrigger("* * * * *"), new SCMTrigger("*/2 * * * *")}
        });
    }

    @Test
    public void testEquals() throws ANTLRException {
        assertEquals(expectedResult, trigger1.equals(trigger2));
    }

    @Test
    public void testHashCode() {
        assertEquals(expectedResult, trigger1.hashCode() == trigger2.hashCode());
    }
}
