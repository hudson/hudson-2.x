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

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class BallColorTest extends TestCase {
    public void testHtmlColor() {
        assertEquals("#EF2929",BallColor.RED.getHtmlBaseColor());
    }
}
