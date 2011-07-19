/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

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
