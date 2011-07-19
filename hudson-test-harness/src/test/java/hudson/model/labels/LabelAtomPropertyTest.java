/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model.labels;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Kohsuke Kawaguchi
 */
public class LabelAtomPropertyTest extends HudsonTestCase {
    public static class LabelAtomPropertyImpl extends LabelAtomProperty {
        public final String abc;

        @DataBoundConstructor
        public LabelAtomPropertyImpl(String abc) {
            this.abc = abc;
        }

        @TestExtension
        public static class DescriptorImpl extends LabelAtomPropertyDescriptor {
            @Override
            public String getDisplayName() {
                return "Test label atom property";
            }
        }
    }

    /**
     * Tests the configuration persistence between disk, memory, and UI.
     */
    public void testConfigRoundtrip() throws Exception {
        LabelAtom foo = hudson.getLabelAtom("foo");
        LabelAtomPropertyImpl old = new LabelAtomPropertyImpl("value");
        foo.getProperties().add(old);
        assertTrue(foo.getConfigFile().exists());
        foo.load(); // make sure load works

        // it should survive the configuration roundtrip
        submit(createWebClient().goTo("label/foo/configure").getFormByName("config"));
        assertEquals(1,foo.getProperties().size());
        assertEqualDataBoundBeans(old, foo.getProperties().get(LabelAtomPropertyImpl.class));
    }
}
