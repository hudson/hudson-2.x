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

package hudson.security;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.Email;
import hudson.model.Hudson;

/**
 * @author Kohsuke Kawaguchi
 */
public class PermissionGroupTest extends HudsonTestCase {
    /**
     * "Overall" persmission group should be always the first.
     */
    @Email("http://www.nabble.com/Master-slave-refactor-td21361880.html")
    public void testOrder() {
        assertSame(PermissionGroup.getAll().get(0),Hudson.PERMISSIONS);
    }
}
