/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   , Alan Harder
 *      
 *
 *******************************************************************************/ 

package hudson.matrix;

import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Alan Harder
 */
public class MatrixTest extends HudsonTestCase {

    /**
     * Test that spaces are encoded as %20 for project name, axis name and axis value.
     */
    public void testSpaceInUrl() {
        MatrixProject mp = new MatrixProject("matrix test");
        MatrixConfiguration mc = new MatrixConfiguration(mp, Combination.fromString("foo bar=baz bat"));
        assertEquals("job/matrix%20test/", mp.getUrl());
        assertTrue("Invalid: " + mc.getUrl(),
                   "job/matrix%20test/foo%20bar=baz%20bat/".equals(mc.getUrl())
                   || "job/matrix%20test/./foo%20bar=baz%20bat/".equals(mc.getUrl()));
    }
}
