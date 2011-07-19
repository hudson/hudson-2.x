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

package hudson;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginManagerTest extends HudsonTestCase {
    @Override
    protected void setUp() throws Exception {
        useLocalPluginManager = true;
        super.setUp();
    }

    /**
     * Manual submission form.
     */
    public void testUpload() throws Exception {
        HtmlPage page = new WebClient().goTo("pluginManager/advanced");
        HtmlForm f = page.getFormByName("uploadPlugin");
        File dir = env.temporaryDirectoryAllocator.allocate();
        File plugin = new File(dir, "tasks.hpi");
        FileUtils.copyURLToFile(getClass().getClassLoader().getResource("plugins/tasks.hpi"),plugin);
        f.getInputByName("name").setValueAttribute(plugin.getAbsolutePath());
        submit(f);

        assertTrue( new File(hudson.getRootDir(),"plugins/tasks.hpi").exists() );
    }

    /**
     * Tests the effect of {@link WithPlugin}.
     */
    @WithPlugin("tasks.hpi")
    public void testWithRecipe() throws Exception {
        assertNotNull(hudson.getPlugin("tasks"));
    }

    /**
     * Makes sure that plugins can see Maven2 plugin that's refactored out in 1.296.
     */
    @WithPlugin("tasks.hpi")
    public void testOptionalMavenDependency() throws Exception {
        PluginWrapper.Dependency m2=null;
        PluginWrapper tasks = hudson.getPluginManager().getPlugin("tasks");
        for( PluginWrapper.Dependency d : tasks.getOptionalDependencies() ) {
            if(d.shortName.equals("maven-plugin")) {
                assertNull(m2);
                m2 = d;
            }
        }
        assertNotNull(m2);

        // this actually doesn't really test what we need, though, because
        // I thought test harness is loading the maven classes by itself.
        // TODO: write a separate test that tests the optional dependency loading
        tasks.classLoader.loadClass(org.eclipse.hudson.legacy.maven.interceptor.AbortException.class.getName());
    }
}
