/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import junit.framework.TestCase;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Quick test for {@link UpdateCenter}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class UpdateCenterTest extends TestCase {
    public void testData() throws IOException {
        // check if we have the internet connectivity. See HUDSON-2095
        try {
            new URL("http://hudson-ci.org/").openStream();
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
