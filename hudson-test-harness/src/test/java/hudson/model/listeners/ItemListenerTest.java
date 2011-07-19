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
*    Alan Harder
 *     
 *
 *******************************************************************************/ 

package hudson.model.listeners;

import hudson.model.Item;

import org.eclipse.hudson.cli.CLI;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Tests for ItemListener events.
 * @author Alan.Harder@sun.com
 */
public class ItemListenerTest extends HudsonTestCase {
    private ItemListener listener;
    private StringBuffer events = new StringBuffer();

    @Override protected void setUp() throws Exception {
        super.setUp();
        listener = new ItemListener() {
            @Override public void onCreated(Item item) {
                events.append('C');
            }
            @Override public void onCopied(Item src, Item item) {
                events.append('Y');
            }
        };
        ItemListener.all().add(0, listener);
    }

    public void testOnCreatedViaCLI() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf);
        new CLI(getURL()).execute(Arrays.asList("create-job","testJob"),
                 new ByteArrayInputStream(("<project><actions/><builders/><publishers/>"
                    + "<buildWrappers/></project>").getBytes()),
                out, out);
        out.flush();
        assertNotNull("job should be created: " + buf, hudson.getItem("testJob"));
        assertEquals("onCreated event should be triggered: " + buf, "C", events.toString());
    }
}
