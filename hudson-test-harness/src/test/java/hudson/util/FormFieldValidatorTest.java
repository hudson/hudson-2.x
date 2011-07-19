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

package hudson.util;

import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidatorTest.BrokenFormValidatorBuilder.DescriptorImpl;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.WithPlugin;

/**
 * @author Kohsuke Kawaguchi
 */
public class FormFieldValidatorTest extends HudsonTestCase {
    @Bug(2771)
    @WithPlugin("tasks.hpi")
    public void test2771() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        new WebClient().getPage(p,"configure");
    }

    public static class BrokenFormValidatorBuilder extends Builder {
        public static final class DescriptorImpl extends BuildStepDescriptor {
            public boolean isApplicable(Class jobType) {
                return true;
            }

            public void doCheckXyz() {
                throw new Error("doCheckXyz is broken");
            }

            public String getDisplayName() {
                return "I have broken form field validation";
            }
        }
    }

    /**
     * Make sure that the validation methods are really called by testing a negative case.
     */
    @Bug(3382)
    public void testNegative() throws Exception {
        DescriptorImpl d = new DescriptorImpl();
        Publisher.all().add(d);
        try {
            FreeStyleProject p = createFreeStyleProject();
            new WebClient().getPage(p,"configure");
            fail("should have failed");
        } catch(AssertionError e) {
            if(e.getMessage().contains("doCheckXyz is broken"))
                ; // expected
            else
                throw e;
        } finally {
            Publisher.all().remove(d);
        }
    }

}
