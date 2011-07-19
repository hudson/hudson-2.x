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
*    Kohsuke Kawaguchi, Red Hat, Inc., Victor Glushenkov
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public abstract class AbstractMavenBuild<P extends AbstractMavenProject<P,B>,B extends AbstractMavenBuild<P,B>> extends AbstractBuild<P, B>  {

    /**
     * Extra verbose debug switch.
     */
    public static boolean debug = false;
    
    protected AbstractMavenBuild(P job) throws IOException {
        super(job);
    }
    
    public AbstractMavenBuild(P job, Calendar timestamp) {
        super(job, timestamp);
    }
    
    public AbstractMavenBuild(P project, File buildDir) throws IOException {
        super(project, buildDir);
    }
 
    
    
}
