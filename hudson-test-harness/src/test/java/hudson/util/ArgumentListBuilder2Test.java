/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       Kohsuke Kawaguchi
 *
 *******************************************************************************/ 

package hudson.util;

import hudson.Launcher.RemoteLauncher;
import hudson.model.Slave;
import org.jvnet.hudson.test.Email;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.StringWriter;

/**
 *
 * @author Kohsuke Kawaguchi
 */
public class ArgumentListBuilder2Test extends HudsonTestCase {
    /**
     * Makes sure {@link RemoteLauncher} properly masks arguments.
     */
    @Email("http://n4.nabble.com/Password-masking-when-running-commands-on-a-slave-tp1753033p1753033.html")
    public void testSlaveMask() throws Exception {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add("java");
        args.addMasked("-version");

        Slave s = createSlave();
        s.toComputer().connect(false).get();
        
        StringWriter out = new StringWriter();
        assertEquals(0,s.createLauncher(new StreamTaskListener(out)).launch().cmds(args).join());
        System.out.println(out);
        assertTrue(out.toString().contains("$ java ********"));
    }
}
