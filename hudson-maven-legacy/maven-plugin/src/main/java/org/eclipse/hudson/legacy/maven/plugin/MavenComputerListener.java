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
*    Kohsuke Kawaguchi, Stephen Connolly
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.Extension;
import hudson.FilePath;
import hudson.maven.agent.Maven21Interceptor;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.Which;
import hudson.slaves.ComputerListener;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.eclipse.hudson.legacy.maven.agent.Main;
import org.eclipse.hudson.legacy.maven.interceptor.AbortException;
import org.eclipse.hudson.legacy.maven3.agent.Maven3Main;
import org.eclipse.hudson.legacy.maven3.interceptor.launcher.Maven3Launcher;

/**
 * When a slave is connected, copy <tt>maven-agent.jar</tt> and <tt>maven-intercepter.jar</tt>
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class MavenComputerListener extends ComputerListener {
    @Override
    public void preOnline(Computer c, Channel channel,FilePath root,  TaskListener listener) throws IOException, InterruptedException {
        PrintStream logger = listener.getLogger();
        copyJar(logger, root, Main.class, "maven-agent");
        copyJar(logger, root, Maven3Main.class, "maven3-agent");
        copyJar(logger, root, Maven3Launcher.class, "maven3-interceptor");
        copyJar(logger, root, AbortException.class, "maven-interceptor");
        copyJar(logger, root, Maven21Interceptor.class, "maven2.1-interceptor");
        copyJar(logger, root, ClassWorld.class, "plexus-classworld");
        
        // copy classworlds 1.1 for maven2 builds
        root.child( "classworlds.jar" ).copyFrom(getClass().getClassLoader().getResource("classworlds.jar"));
        logger.println("Copied classworlds.jar");
    }

    /**
     * Copies a jar file from the master to slave.
     */
    private void copyJar(PrintStream log, FilePath dst, Class<?> representative, String seedName) throws IOException, InterruptedException {
        // in normal execution environment, the master should be loading 'representative' from this jar, so
        // in that way we can find it.
        File jar = Which.jarFile(representative);

        if(jar.isDirectory()) {
            // but during the development and unit test environment, we may be picking the class up from the classes dir
            Zip zip = new Zip();
            zip.setBasedir(jar);
            File t = File.createTempFile(seedName, "jar");
            t.delete();
            zip.setDestFile(t);
            zip.setProject(new Project());
            zip.execute();
            jar = t;
        }

        new FilePath(jar).copyTo(dst.child(seedName +".jar"));
        log.println("Copied "+seedName+".jar");
    }
}
