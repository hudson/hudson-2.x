/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.maven;

import hudson.model.Hudson;
import hudson.remoting.Which;
import junit.framework.Test;
import junit.framework.TestCase;
import org.jvnet.hudson.test.JellyTestSuiteBuilder;
import java.io.File;

/**
 * Runs Jelly checks on maven-plugin.
 *
 * @author Andrew Bayer
 */
public class MavenJellyTest extends TestCase {
    public static Test suite() throws Exception {
        return JellyTestSuiteBuilder.build(Which.jarFile(MavenModuleSet.class));
    }
}
