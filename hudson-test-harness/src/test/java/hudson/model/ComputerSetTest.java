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
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class ComputerSetTest extends HudsonTestCase {
    //TODO fix me (it fails on CI)
    @Bug(2821)
    public void ignore_testPageRendering() throws Exception {
        HudsonTestCase.WebClient client = new WebClient();
        createSlave();
        client.goTo("computer");
    }

    /**
     * Tests the basic UI behavior of the node monitoring
     */
    public void testConfiguration() throws Exception {
        HudsonTestCase.WebClient client = new WebClient();
        HtmlForm form = client.goTo("computer/configure").getFormByName("config");
        submit(form);
    }
}
