/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *      
 *
 *******************************************************************************/ 

package hudson.cli;

import hudson.model.FreeStyleProject;
import java.io.IOException;

import org.eclipse.hudson.cli.CLI;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 *
 * @author Winston Prakash (converted from original groovy test)
 * 
 */
public class EnableJobCommandTest extends HudsonTestCase {
    
    public void test1() throws IOException, InterruptedException{
        FreeStyleProject p = createFreeStyleProject();

        CLI cli = new CLI(getURL());

        cli.execute("disable-job",p.getName());
        assertTrue(p.isDisabled());
        cli.execute("enable-job",p.getName());
        assertFalse(p.isDisabled());
    }
    
}
