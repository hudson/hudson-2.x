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

package org.jvnet.hudson.test;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.scm.NullSCM;
import hudson.scm.SCM;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * {@link SCM} useful for testing that extracts the given resource as a zip file.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExtractResourceSCM extends NullSCM {
    private final URL zip;

    public ExtractResourceSCM(URL zip) {
        if(zip==null)
            throw new IllegalArgumentException();
        this.zip = zip;
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changeLogFile) throws IOException, InterruptedException {
    	if (workspace.exists()) {
            listener.getLogger().println("Deleting existing workspace " + workspace.getRemote());
    		workspace.deleteRecursive();
    	}
        listener.getLogger().println("Staging "+zip);
        workspace.unzipFrom(zip.openStream());
        return true;
    }
}
