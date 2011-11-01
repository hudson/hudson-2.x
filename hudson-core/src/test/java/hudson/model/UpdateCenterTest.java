/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Nikita Levyankov
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Quick test for {@link UpdateCenter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class UpdateCenterTest {

    @Test
    public void testData() throws IOException {
        // check if we have the internet connectivity. See HUDSON-2095
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://hudson-ci.org/").openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(10000); //set timeout to 10 seconds
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Skipping this test. Page doesn't exists");
                return;
            }
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Skipping this test. Timeout exception");
            return;
        } catch (IOException e) {
            System.out.println("Skipping this test. No internet connectivity");
            return;
        }

        URL url = new URL("http://hudson-ci.org/update-center.json?version=build");
        String jsonp = IOUtils.toString(url.openStream());
        String json = jsonp.substring(jsonp.indexOf('(')+1,jsonp.lastIndexOf(')'));

        UpdateSite us = new UpdateSite("default", url.toExternalForm());
        UpdateSite.Data data = us.new Data(JSONObject.fromObject(json));
        assertTrue(data.core.url.startsWith("http://hudson-ci.org/"));
        assertTrue(data.plugins.containsKey("rake"));
        System.out.println(data.core.url);
    }
}
