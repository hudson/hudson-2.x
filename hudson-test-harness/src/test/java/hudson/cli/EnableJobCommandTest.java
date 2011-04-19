/*
 * The MIT License
 *
 * Copyright 2011 Hudson.
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
package hudson.cli;

import hudson.model.FreeStyleProject;
import java.io.IOException;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 *
 * @author Winston Prakash (converted from original groovy test)
 * 
 */
public class EnableJobCommandTest extends HudsonTestCase {
    
    public void test1() throws IOException, InterruptedException{
        FreeStyleProject p = createFreeStyleProject();

        CLI cli = new CLI(getURL());

        cli.execute("disable-job",p.getName());
        assertTrue(p.isDisabled());
        cli.execute("enable-job",p.getName());
        assertFalse(p.isDisabled());
    }
    
}
