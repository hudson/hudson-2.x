/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.tools;

import hudson.Functions;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.tasks.Maven;
import hudson.tasks.BatchFile;
import hudson.tasks.Ant;
import hudson.tasks.Shell;
import hudson.tasks.Ant.AntInstallation;
import hudson.tasks.Maven.MavenInstallation;
import hudson.EnvVars;

import java.io.IOException;

import junit.framework.Assert;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;
import org.jvnet.hudson.test.ExtractResourceSCM;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Build;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.JDK;
import hudson.model.Result;
import hudson.model.Run;

import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;

/**
 * This class tests that environment variables from node properties are applied,
 * and that the priority is maintained: parameters > slave node properties >
 * master node properties
 */
public class ToolLocationNodePropertyTest extends HudsonTestCase {

    private DumbSlave slave;
    private FreeStyleProject project;

    public void testFormRoundTrip() throws Exception {

        MavenInstallation.DescriptorImpl mavenDescriptor = hudson.getDescriptorByType(MavenInstallation.DescriptorImpl.class);
        mavenDescriptor.setInstallations(new MavenInstallation("maven", "XXX", NO_PROPERTIES));
        AntInstallation.DescriptorImpl antDescriptor = hudson.getDescriptorByType(AntInstallation.DescriptorImpl.class);
        antDescriptor.setInstallations(new AntInstallation("ant", "XXX", NO_PROPERTIES));
        JDK.DescriptorImpl jdkDescriptor = hudson.getDescriptorByType(JDK.DescriptorImpl.class);
        jdkDescriptor.setInstallations(new JDK("jdk", "XXX"));

        ToolLocationNodeProperty property = new ToolLocationNodeProperty(
                new ToolLocationNodeProperty.ToolLocation(jdkDescriptor, "jdk", "foobar"),
                new ToolLocationNodeProperty.ToolLocation(mavenDescriptor, "maven", "barzot"),
                new ToolLocationNodeProperty.ToolLocation(antDescriptor, "ant", "zotfoo"));
        slave.getNodeProperties().add(property);

        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(slave, "configure");
        HtmlForm form = page.getFormByName("config");
        submit(form);

        Assert.assertEquals(1, slave.getNodeProperties().toList().size());

        ToolLocationNodeProperty prop = slave.getNodeProperties().get(ToolLocationNodeProperty.class);
        Assert.assertEquals(3, prop.getLocations().size());

        ToolLocationNodeProperty.ToolLocation location = prop.getLocations().get(0);
        Assert.assertEquals(jdkDescriptor, location.getType());
        Assert.assertEquals("jdk", location.getName());
        Assert.assertEquals("foobar", location.getHome());

        location = prop.getLocations().get(1);
        Assert.assertEquals(mavenDescriptor, location.getType());
        Assert.assertEquals("maven", location.getName());
        Assert.assertEquals("barzot", location.getHome());

        location = prop.getLocations().get(2);
        Assert.assertEquals(antDescriptor, location.getType());
        Assert.assertEquals("ant", location.getName());
        Assert.assertEquals("zotfoo", location.getHome());
    }

    public void testMaven() throws Exception {
        MavenInstallation maven = configureDefaultMaven();
        String mavenPath = maven.getHome();
        Hudson.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(new MavenInstallation("maven", "THIS IS WRONG", NO_PROPERTIES));

        project.getBuildersList().add(new Maven("--version", "maven"));
        configureDumpEnvBuilder();

        Build build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.FAILURE, build);

        ToolLocationNodeProperty property = new ToolLocationNodeProperty(
                new ToolLocationNodeProperty.ToolLocation(hudson.getDescriptorByType(MavenInstallation.DescriptorImpl.class), "maven", mavenPath));
        slave.getNodeProperties().add(property);

        build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, build);
    }

    private void configureDumpEnvBuilder() throws IOException {
        if(Functions.isWindows())
            project.getBuildersList().add(new BatchFile("set"));
        else
            project.getBuildersList().add(new Shell("export"));
    }

    public void testAnt() throws Exception {
        Ant.AntInstallation ant = configureDefaultAnt();
        String antPath = ant.getHome();
        Hudson.getInstance().getDescriptorByType(Ant.DescriptorImpl.class).setInstallations(new AntInstallation("ant", "THIS IS WRONG"));

        project.setScm(new SingleFileSCM("build.xml", "<project name='foo'/>"));
        project.getBuildersList().add(new Ant("-version", "ant", null,null,null));
        configureDumpEnvBuilder();

        Build build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.FAILURE, build);

        ToolLocationNodeProperty property = new ToolLocationNodeProperty(
                new ToolLocationNodeProperty.ToolLocation(hudson.getDescriptorByType(AntInstallation.DescriptorImpl.class), "ant", antPath));
        slave.getNodeProperties().add(property);

        build = project.scheduleBuild2(0).get();
        System.out.println(build.getLog());
        assertBuildStatus(Result.SUCCESS, build);
    }

    public void testNativeMaven() throws Exception {
        MavenInstallation maven = configureDefaultMaven();
        String mavenPath = maven.getHome();
        Hudson.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(new MavenInstallation("maven", "THIS IS WRONG", NO_PROPERTIES));

        MavenModuleSet project = createMavenProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource(
                "/simple-projects.zip")));
        project.setAssignedLabel(slave.getSelfLabel());
        project.setJDK(hudson.getJDK("default"));

        project.setMaven("maven");
        project.setGoals("clean");

        Run build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.FAILURE, build);

        ToolLocationNodeProperty property = new ToolLocationNodeProperty(
                new ToolLocationNodeProperty.ToolLocation(hudson.getDescriptorByType(MavenInstallation.DescriptorImpl.class), "maven", mavenPath));
        slave.getNodeProperties().add(property);

        build = project.scheduleBuild2(0).get();
        System.out.println(build.getLog());
        assertBuildStatus(Result.SUCCESS, build);

    }

    // //////////////////////// setup //////////////////////////////////////////

    public void setUp() throws Exception {
        super.setUp();
        EnvVars env = new EnvVars();
        // we don't want Maven, Ant, etc. to be discovered in the path for this test to work,
        // but on Unix these tools rely on other basic Unix tools (like env) for its operation,
        // so empty path breaks the test.
        env.put("PATH", "/bin:/usr/bin");
        env.put("M2_HOME", "empty");
        slave = createSlave(new LabelAtom("slave"), env);
        project = createFreeStyleProject();
        project.setAssignedLabel(slave.getSelfLabel());
    }
}
