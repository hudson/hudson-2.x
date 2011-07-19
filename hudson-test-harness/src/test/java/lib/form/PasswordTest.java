/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc..
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

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class PasswordTest extends HudsonTestCase implements Describable<PasswordTest> {
    public Secret secret;

    public void test1() throws Exception {
        secret = Secret.fromString("secret");
        HtmlPage p = createWebClient().goTo("self/test1");
        String value = ((HtmlInput)p.getElementById("password")).getValueAttribute();
        assertFalse("password shouldn't be plain text",value.equals("secret"));
        assertEquals("secret",Secret.fromString(value).getPlainText());
    }

    public DescriptorImpl getDescriptor() {
        return hudson.getDescriptorByType(DescriptorImpl.class);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<PasswordTest> {
        public String getDisplayName() {
            return null;
        }
    }
}
