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

package hudson.maven.reporters;

import org.jvnet.hudson.test.HudsonTestCase;
import hudson.maven.MavenProjectTest;

/**
 * @author Kohsuke Kawaguchi
 */
public class MavenSiteArchiverTest extends HudsonTestCase {
    /**
     * Makes sure that the site archiving happens automatically.
     * The actual test resides in {@link MavenProjectTest#testSiteBuild()} 
     */
    public void testSiteArchiving() throws Exception {
    }
}
