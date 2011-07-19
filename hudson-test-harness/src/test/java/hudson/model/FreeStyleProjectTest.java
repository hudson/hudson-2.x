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

package hudson.model;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.List;
import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class FreeStyleProjectTest extends HudsonTestCase {
    /**
     * Tests a trivial configuration round-trip.
     *
     * The goal is to catch a P1-level issue that prevents all the form submissions to fail.
     */
    public void testConfigSubmission() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        Shell shell = new Shell("echo hello");
        project.getBuildersList().add(shell);

        // emulate the user behavior
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(project,"configure");

        HtmlForm form = page.getFormByName("config");
        submit(form);

        List<Builder> builders = project.getBuilders();
        assertEquals(1,builders.size());
        assertEquals(Shell.class,builders.get(0).getClass());
        assertEquals("echo hello",((Shell)builders.get(0)).getCommand().trim());
        assertTrue(builders.get(0)!=shell);
    }

    /**
     * Make sure that the pseudo trigger configuration works.
     */
    @Bug(2778)
    public void testUpstreamPseudoTrigger() throws Exception {
        pseudoTriggerTest(createMavenProject(), createFreeStyleProject());
    }

    @Bug(2778)
    public void testUpstreamPseudoTrigger2() throws Exception {
        pseudoTriggerTest(createFreeStyleProject(), createFreeStyleProject());
    }

    @Bug(2778)
    public void testUpstreamPseudoTrigger3() throws Exception {
        pseudoTriggerTest(createMatrixProject(), createFreeStyleProject());
    }

    private void pseudoTriggerTest(AbstractProject up, AbstractProject down) throws Exception {
        HtmlForm form = new WebClient().getPage(down, "configure").getFormByName("config");
        form.getInputByName("pseudoUpstreamTrigger").setChecked(true);
        form.getInputByName("upstreamProjects").setValueAttribute(up.getName());
        submit(form);

        // make sure this took effect
        assertTrue(up.getDownstreamProjects().contains(down));
        assertTrue(down.getUpstreamProjects().contains(up));

        // round trip again and verify that the configuration is still intact.
        submit(new WebClient().getPage(down, "configure").getFormByName("config"));
        assertTrue(up.getDownstreamProjects().contains(down));
        assertTrue(down.getUpstreamProjects().contains(up));
    }

    /**
     * Custom workspace and concurrent build had a bad interaction.
     */
    @Bug(4206)
    public void testCustomWorkspaceAllocation() throws Exception {
        FreeStyleProject f = createFreeStyleProject();
        File d = createTmpDir();
        f.setCustomWorkspace(d.getPath());
        buildAndAssertSuccess(f);
    }

    /**
     * Custom workspace and variable expansion.
     */
    @Bug(3997)
    public void testCustomWorkspaceVariableExpansion() throws Exception {
        FreeStyleProject f = createFreeStyleProject();
        File d = new File(createTmpDir(),"${JOB_NAME}");
        f.setCustomWorkspace(d.getPath());
        FreeStyleBuild b = buildAndAssertSuccess(f);

        String path = b.getWorkspace().getRemote();
        System.out.println(path);
        assertFalse(path.contains("${JOB_NAME}"));
        assertTrue(b.getWorkspace().getName().equals(f.getName()));
    }
}
