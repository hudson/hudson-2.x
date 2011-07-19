/*******************************************************************************
 *
 * Copyright (c) 2004-2010, CollabNet.
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

package lib.form;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Tests the handling of @nameRef in the form tree.
 *
 * @author Kohsuke Kawaguchi
 */
public class NameRefTest extends HudsonTestCase {
    public void test1() throws Exception {
        hudson.setCrumbIssuer(null);
        HtmlPage p = createWebClient().goTo("self/test1");
        submit(p.getFormByName("config"));
    }

    public HttpResponse doSubmitTest1(StaplerRequest req) throws Exception {
        JSONObject f = req.getSubmittedForm();
        System.out.println(f);
        assertEquals("{\"foo\":{\"bar\":{\"zot\":\"zot\"}}}",f.toString());
        return HttpResponses.ok();
    }
}
