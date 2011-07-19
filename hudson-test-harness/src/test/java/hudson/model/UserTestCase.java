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

package hudson.model;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jvnet.hudson.test.HudsonTestCase;

public class UserTestCase extends HudsonTestCase {

    public static class UserPropertyImpl extends UserProperty {

        private final String testString;
        private UserPropertyDescriptor descriptorImpl = new UserPropertyDescriptorImpl();
        
        public UserPropertyImpl(String testString) {
            this.testString = testString;
        }
        
        public String getTestString() {
            return testString;
        }

        @Override
        public UserPropertyDescriptor getDescriptor() {
            return descriptorImpl;
        }
        
        public static class UserPropertyDescriptorImpl extends UserPropertyDescriptor {
            @Override
            public UserProperty newInstance(User user) {
                return null;
            }

            @Override
            public String getDisplayName() {
                return "Property";
            }
        }
    }

    /**
     * Asserts that bug# is fixed.
     */
    public void testUserPropertySummaryIsShownInUserPage() throws Exception {
        
        UserProperty property = new UserPropertyImpl("NeedleInPage");
        UserProperty.all().add(property.getDescriptor());
        
        User user = User.get("user-test-case");
        user.addProperty(property);
        
        HtmlPage page = new WebClient().goTo("user/user-test-case");
        WebAssert.assertTextPresent(page, "NeedleInPage");
    }
}
