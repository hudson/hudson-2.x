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

package org.hudsonci.utils.hamcrest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class URLRespondsWithStatusMatcher extends TypeSafeMatcher<URL> {

    private int statusCode = -2; // since -1 means the response was non-http-valid
    private int timeout = 0; // negative timeout would throw iae, default infinite
    private int receivedStatusCode = 0;
    private boolean timedOut = false;
    private Exception e;
    private String urlString;

    public URLRespondsWithStatusMatcher(final int statusCode) {
        this(statusCode, 0);
    }

    public URLRespondsWithStatusMatcher(final int statusCode, final int timeout) {
        if(timeout < 0){
            throw new IllegalArgumentException("timeout cannot be negative");
        }
        if(statusCode < -1){
            throw new IllegalArgumentException("status code cannot be less than negative one");
        }
        this.statusCode = statusCode;
        this.timeout = timeout;
    }

    public void describeTo(Description description) {
        if(timedOut){
            description.appendValue(urlString).appendText(" to connect within ").appendValue(timeout).appendText("ms");
        } else if (e != null){
            description.appendValue(urlString).appendText(" to successfully connect");
        } else {
            description.appendValue(urlString).appendText(" to respond with ").appendValue(statusCode);
        }
    }


    @Override
    protected void describeMismatchSafely(URL item, Description mismatchDescription) {
        if(timedOut){
            mismatchDescription.appendText("took longer.");
            if(this.e != null){
                mismatchDescription.appendText(this.e.getMessage());
            }
        } else if (e != null){
            mismatchDescription.appendText("got ").appendValue(this.e);
            // description.appendText("got exception ").appendValue(e);
        } else {
            mismatchDescription.appendText("was ").appendValue(receivedStatusCode);
        }

    }

    @Override
    protected boolean matchesSafely(URL item) {
        this.urlString = item.toString();
        try {
            URL url = new URL(item.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(timeout > -1){
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setReadTimeout(timeout);
            }
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            this.receivedStatusCode = urlConnection.getResponseCode();
        } catch (SocketTimeoutException ste){
            timedOut = true;
            this.e = ste;
        } catch (IOException ioe) {
            this.e = ioe;
        } catch (Exception e){
            this.e = e;
        }
        return (!timedOut && e == null && this.receivedStatusCode == this.statusCode);

    }

}
