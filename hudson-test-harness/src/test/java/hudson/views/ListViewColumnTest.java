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

package hudson.views;

import hudson.model.ListView;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListViewColumnTest extends HudsonTestCase {
    public void testCreateView() throws Exception {
        hudson.addView(new ListView("test"));
        submit(createWebClient().goTo("view/test/configure").getFormByName("viewConfig"));
    }
}
