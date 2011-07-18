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
 *
 *******************************************************************************/ 

package hudson.model;

import org.jvnet.hudson.test.HudsonTestCase;

import java.io.StringReader;

/**
 * @author Kohsuke Kawaguchi
 */
public class ExternalRunTest extends HudsonTestCase {
    public void test1() throws Exception {
        ExternalJob p = hudson.createProject(ExternalJob.class, "test");
        ExternalRun b = p.newBuild();
        b.acceptRemoteSubmission(new StringReader(
            "<run><log content-encoding='UTF-8'>AAAAAAAA</log><result>0</result><duration>100</duration></run>"
        ));
        assertEquals(b.getResult(),Result.SUCCESS);
        assertEquals(b.getDuration(),100);

        b = p.newBuild();
        b.acceptRemoteSubmission(new StringReader(
            "<run><log content-encoding='UTF-8'>AAAAAAAA</log><result>1</result>"
        ));
        assertEquals(b.getResult(),Result.FAILURE);
    }
}
