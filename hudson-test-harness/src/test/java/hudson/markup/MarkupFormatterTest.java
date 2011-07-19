/*******************************************************************************
 *
 * Copyright (c) 2010, CloudBees, Inc.
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

package hudson.markup;

import hudson.security.AuthorizationStrategy.Unsecured;
import hudson.security.HudsonPrivateSecurityRealm;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Kohsuke Kawaguchi
 */
public class MarkupFormatterTest extends HudsonTestCase {
    public void testConfigRoundtrip() throws Exception {
        hudson.setSecurityRealm(new HudsonPrivateSecurityRealm(false));
        hudson.setAuthorizationStrategy(new Unsecured());
        hudson.setMarkupFormatter(new DummyMarkupImpl("hello"));
        configRoundtrip();

        assertEquals("hello", ((DummyMarkupImpl)hudson.getMarkupFormatter()).prefix);
    }

    public static class DummyMarkupImpl extends MarkupFormatter {
        public final String prefix;
        @DataBoundConstructor
        public DummyMarkupImpl(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void translate(String markup, Writer output) throws IOException {
            output.write(prefix+"["+markup+"]");
        }

        @TestExtension
        public static class DescriptorImpl extends MarkupFormatterDescriptor {
            @Override
            public String getDisplayName() {
                return "dummy";
            }
        }
    }
}
