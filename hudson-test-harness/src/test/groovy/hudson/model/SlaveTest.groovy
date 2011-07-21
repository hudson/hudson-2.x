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
#         Kohsuke Kawaguchi
#
#**************************************************************************/ 
package hudson.model

import hudson.slaves.DumbSlave
import org.jvnet.hudson.test.GroovyHudsonTestCase

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class SlaveTest extends GroovyHudsonTestCase {
    /**
     * Makes sure that a form validation method gets inherited.
     */
    void testFormValidation() {
        executeOnServer {
            assertNotNull(hudson.getDescriptor(DumbSlave.class).getCheckUrl("remoteFS"))
        }
    }
}
