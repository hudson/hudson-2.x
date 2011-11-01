/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Anton Kozak
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
package hudson.matrix;

import java.util.Map;
import java.util.HashMap;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Kohsuke Kawaguchi
 */
public class CombinationTest{
    AxisList axes = new AxisList(
            new Axis("a","X","x"),
            new Axis("b","Y","y"));

    @Test
    @SuppressWarnings({"RedundantStringConstructorCall"})
    public void testEval() {
        Map<String,String> r = new HashMap<String, String>();
        r.put("a","X");
        r.put("b",new String("Y")); // make sure this 'Y' is not the same object as literal "Y".
        Combination c = new Combination(r);

        r.put("a","x");
        Combination d = new Combination(r);

        assertTrue(eval(c, null));
        assertTrue(eval(c,"    "));
        assertTrue(eval(c,"true"));
        assertTrue(eval(c,"a=='X'"));
        assertTrue(eval(c,"b=='Y'"));
        assertTrue(eval(c,"(a=='something').implies(b=='other')"));
        assertTrue(eval(c,"index%2==0")^eval(d,"index%2==0"));
        assertTrue(eval(c,"index%2==1")^eval(d,"index%2==1"));
    }

    private boolean eval(Combination c, String exp) {
        return c.evalGroovyExpression(axes, exp);
    }
}
