/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.QueryParameter;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.Extension;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class ValidateButtonTest extends HudsonTestCase implements Describable<ValidateButtonTest> {

    public void test1() throws Exception {
        DescriptorImpl d = getDescriptor();
        d.test1Outcome = new Exception(); // if doValidateTest1() doesn't get invoked, we want to know.
        HtmlPage p = createWebClient().goTo("self/test1");
        p.getFormByName("config").getButtonByCaption("test").click();
        if (d.test1Outcome!=null)
            throw d.test1Outcome;
    }

    public DescriptorImpl getDescriptor() {
        return hudson.getDescriptorByType(DescriptorImpl.class);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ValidateButtonTest> {
        private Exception test1Outcome;

        public String getDisplayName() {
            return null;
        }

        public void doValidateTest1(@QueryParameter("a") String a, @QueryParameter("b") boolean b,
                                    @QueryParameter("c") boolean c, @QueryParameter("d") String d,
                                    @QueryParameter("e") String e) {
            try {
                assertEquals("avalue",a);
                assertTrue(b);
                assertFalse(c);
                assertEquals("dvalue",d);
                assertEquals("e2",e);
                test1Outcome = null;
            } catch (Exception t) {
                test1Outcome = t;
            }
        }
    }
}
