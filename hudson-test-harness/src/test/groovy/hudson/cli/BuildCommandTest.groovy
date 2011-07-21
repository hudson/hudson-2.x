/**************************************************************************
#
# Copyright (C) 2004-2009 Oracle Corporation
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#         
#
#**************************************************************************/ 
package hudson.cli

import org.jvnet.hudson.test.HudsonTestCase
import hudson.tasks.Shell
import hudson.util.OneShotEvent
import org.jvnet.hudson.test.TestBuilder
import hudson.model.AbstractBuild
import hudson.Launcher
import hudson.model.BuildListener
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.ParametersAction

/**
 * {@link BuildCommand} test.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildCommandTest extends HudsonTestCase {
    /**
     * Just schedules a build and return.
     */
    void testAsync() {
        def p = createFreeStyleProject();
        def started = new OneShotEvent();
        def completed = new OneShotEvent();
        p.buildersList.add([perform: {AbstractBuild build, Launcher launcher, BuildListener listener ->
            started.signal();
            completed.block();
            return true;
        }] as TestBuilder);

        // this should be asynchronous
        assertEquals(0,new CLI(getURL()).execute(["build", p.name]))
        started.block();
        assertTrue(p.getBuildByNumber(1).isBuilding())
        completed.signal();
    }

    /**
     * Tests synchronous execution.
     */
    void testSync() {
        def p = createFreeStyleProject();
        p.buildersList.add(new Shell("sleep 3"));

        new CLI(getURL()).execute(["build","-s",p.name])
        assertFalse(p.getBuildByNumber(1).isBuilding())
    }

    void testParameters() {
        def p = createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty([new StringParameterDefinition("key",null)]));

        new CLI(getURL()).execute(["build","-s","-p","key=foobar",p.name]);
        def b = assertBuildStatusSuccess(p.getBuildByNumber(1));
        assertEquals("foobar",b.getAction(ParametersAction.class).getParameter("key").value);        
    }
}
