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

package org.hudsonci.utils.junit.ext;

import static org.hudsonci.utils.hamcrest.URLMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.net.MalformedURLException;
import java.net.URL;

import com.googlecode.junit.ext.checkers.Checker;

public class URLIsReachable implements Checker {

    private URL url; // arg 0
    private int timeout = 10 * 1000; // arg 1
    private int statusCode = 200; // arg 2
    private String message = ""; // arg 3, ideally the test name

    public URLIsReachable(final String urlString) throws MalformedURLException {
        final String[] args = new String[] { urlString };
        init(args);
    }

    public URLIsReachable(final String[] args) throws MalformedURLException {
        init(args);
    }

    private void init(String[] args) throws MalformedURLException {
        this.url = new URL(args[0]); // required
        if (args.length >= 2) {
            timeout = Integer.parseInt(args[1]);
        }
        if (args.length >= 3) {
            statusCode = Integer.parseInt(args[2]);
        }
        if (args.length == 4) {
            this.message = args[3];
        }
    }

    public boolean satisfy() {
        try {
            assertThat(this.url, respondsWithStatusWithin(this.statusCode, this.timeout));
        } catch (AssertionError ae) {
            System.err.println("[WARN] "
                    + String.format("%s caused a test to be ignored %s, %s", this.getClass().getSimpleName(),
                            this.message, ae.getMessage()));
            return false;
        }
        return true;
    }

}
