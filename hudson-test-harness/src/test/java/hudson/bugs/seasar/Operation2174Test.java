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

package hudson.bugs.seasar;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import hudson.tasks.BuildTrigger.DescriptorImpl;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.Collections;

/**
 * See http://ml.seasar.org/archives/operation/2008-November/004003.html
 *
 * @author Kohsuke Kawaguchi
 */
public class Operation2174Test extends HudsonTestCase {
    /**
     * Upstream/downstream relationship lost.
     */
    public void testBuildChains() throws Exception {
        FreeStyleProject up = createFreeStyleProject("up");
        MavenModuleSet dp = createMavenProject("dp");

        // designate 'dp' as the downstream in 'up'
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(up,"configure");

        HtmlForm form = page.getFormByName("config");

        // configure downstream build
        DescriptorImpl btd = hudson.getDescriptorByType(DescriptorImpl.class);
        form.getInputByName(btd.getJsonSafeClassName()).click();
        form.getInputByName("buildTrigger.childProjects").setValueAttribute("dp");
        submit(form);

        // verify that the relationship is set up
        BuildTrigger trigger = up.getPublishersList().get(BuildTrigger.class);
        assertEquals(trigger.getChildProjects(), Collections.singletonList(dp));

        // now go ahead and edit the downstream
        page = webClient.getPage(dp,"configure");
        form = page.getFormByName("config");
        submit(form);

        // verify that the relationship is set up
        trigger = up.getPublishersList().get(BuildTrigger.class);
        assertNotNull(trigger);
        assertEquals(trigger.getChildProjects(), Collections.singletonList(dp));
    }
}
