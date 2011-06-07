/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
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
package org.jvnet.hudson.test.junit;

import junit.framework.TestCase;

/**
 * {@link TestCase} implementation that has already failed.
 * Used to represent a problem happened during a test suite construction.
 *
 * @author Kohsuke Kawaguchi
 */
public class FailedTest extends TestCase {
    /**
     * The failure. If null, the test will succeed, despite the class name.
     */
    private final Throwable problem;

    public FailedTest(String name, Throwable problem) {
        super(name);
        this.problem = problem;
    }

    public FailedTest(Class name, Throwable problem) {
        this(name.getName(),problem);
    }

    @Override
    protected void runTest() throws Throwable {
        if (problem!=null)
            throw problem;
    }
}
