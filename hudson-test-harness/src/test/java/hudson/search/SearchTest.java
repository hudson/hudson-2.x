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

package hudson.search;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.AlertHandler;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.Bug;

/**
 * @author Kohsuke Kawaguchi
 */
public class SearchTest extends HudsonTestCase {
    /**
     * No exact match should result in a failure status code.
     */
    public void testFailure() throws Exception {
        try {
            search("no-such-thing");
            fail("404 expected");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404,e.getResponse().getStatusCode());
        }
    }

    /**
     * Makes sure the script doesn't execute.
     */
    @Bug(3415)
    public void testXSS() throws Exception {
        try {
            WebClient wc = new WebClient();
            wc.setAlertHandler(new AlertHandler() {
                public void handleAlert(Page page, String message) {
                    throw new AssertionError();
                }
            });
            wc.search("<script>alert('script');</script>");
            fail("404 expected");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404,e.getResponse().getStatusCode());
        }
    }
}
