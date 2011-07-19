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

package hudson.slaves;

import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class ComputerConnectorTest extends HudsonTestCase {
    public void testConfigRoundtrip() throws Exception {
        CommandConnector cc = new CommandConnector("abc def");
        assertEqualDataBoundBeans(cc,configRoundtrip(cc));
    }
}
