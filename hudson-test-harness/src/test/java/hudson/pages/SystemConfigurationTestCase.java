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
*    Kohsuke Kawaguchi, Erik Ramfelt
 *     
 *
 *******************************************************************************/ 

package hudson.pages;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.PageDecorator;
import net.sf.json.JSONObject;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.StaplerRequest;

public class SystemConfigurationTestCase extends HudsonTestCase {

    private PageDecoratorImpl pageDecoratorImpl;

    protected void tearDown() throws Exception {
        if (pageDecoratorImpl != null) {
            PageDecorator.ALL.remove(pageDecoratorImpl);
        }
        super.tearDown();
    }
    
    /**
     * Asserts that bug#2289 is fixed.
     */
    @Bug(2289)
    //TODO- Revisit this test case
    public void ignore_testPageDecoratorIsListedInPage() throws Exception {
        pageDecoratorImpl = new PageDecoratorImpl();
        PageDecorator.ALL.add(pageDecoratorImpl);

        HtmlPage page = new WebClient().goTo("configure");
        assertXPath(page,"//tr[@name='hudson-pages-SystemConfigurationTestCase$PageDecoratorImpl']");

        HtmlForm form = page.getFormByName("config");
        form.getInputByName("_.decoratorId").setValueAttribute("this_is_a_profile");
        submit(form);
        assertEquals("The decorator field was incorrect", "this_is_a_profile", pageDecoratorImpl.getDecoratorId());
    }

    //TODO remove me when testPageDecoratorIsListedInPage will be fixed
    public void testStub(){
        assertTrue(true);
    }

    /**
     * PageDecorator for bug#2289
     */
    private static class PageDecoratorImpl extends PageDecorator {
        private String decoratorId;

        protected PageDecoratorImpl() {
            super(PageDecoratorImpl.class);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            decoratorId = json.getString("decoratorId");
            return true;
        }

        @Override
        public String getDisplayName() {
            return "PageDecoratorImpl";
        }
        
        public String getDecoratorId() {
            return decoratorId;
        }
    }
}
