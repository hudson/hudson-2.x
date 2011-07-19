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
*    Kohsuke Kawaguchi, Olivier Lamy
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.model.Run.RunnerAbortedException;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.Which;
import hudson.tasks.Maven.MavenInstallation;
import hudson.util.ArgumentListBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.hudson.legacy.maven3.agent.Maven3Main;
import org.eclipse.hudson.legacy.maven3.interceptor.launcher.Maven3Launcher;

/**
 * @author Olivier Lamy
 *
 */
public class Maven3ProcessFactory extends AbstractMavenProcessFactory implements ProcessCache.Factory
{

    Maven3ProcessFactory(MavenModuleSet mms, Launcher launcher, EnvVars envVars, FilePath workDir) {
        super( mms, launcher, envVars, workDir );
    }
    /**
     * Builds the command line argument list to launch the maven process.
     *
     */
    protected ArgumentListBuilder buildMavenAgentCmdLine(BuildListener listener,int tcpPort) throws IOException, InterruptedException {
        MavenInstallation mvn = getMavenInstallation(listener);
        if(mvn==null) {
            listener.error("Maven version is not configured for this project. Can't determine which Maven to run");
            throw new RunnerAbortedException();
        }
        if(mvn.getHome()==null) {
            listener.error("Maven '%s' doesn't have its home set",mvn.getName());
            throw new RunnerAbortedException();
        }

        // find classworlds.jar
        String classWorldsJar = getLauncher().getChannel().call(new GetClassWorldsJar(mvn.getHome(),listener));

        boolean isMaster = getCurrentNode()== Hudson.getInstance();
        FilePath slaveRoot=null;
        if(!isMaster)
            slaveRoot = getCurrentNode().getRootPath();

        ArgumentListBuilder args = new ArgumentListBuilder();
        JDK jdk = getJava(listener);
        if(jdk==null) {
            args.add("java");
        } else {
            args.add(jdk.getHome()+"/bin/java"); // use JDK.getExecutable() here ?
        }

        if(MavenProcessFactory.debugPort!=0)
            args.add("-Xrunjdwp:transport=dt_socket,server=y,address="+MavenProcessFactory.debugPort);
        if(MavenProcessFactory.yjp)
            args.add("-agentlib:yjpagent=tracing");

        args.addTokenized(getMavenOpts());
        
        args.add("-cp");
        args.add(
            (isMaster? Which.jarFile(Maven3Main.class).getAbsolutePath():slaveRoot.child("maven3-agent.jar").getRemote())+
            (getLauncher().isUnix()?":":";")+classWorldsJar);
        args.add(Maven3Main.class.getName());

        // M2_HOME
        args.add(mvn.getHome());

        // remoting.jar
        String remotingJar = getLauncher().getChannel().call(new GetRemotingJar());
        if(remotingJar==null) {// this shouldn't be possible, but there are still reports indicating this, so adding a probe here.
            listener.error("Failed to determine the location of slave.jar");
            throw new RunnerAbortedException();
        }
        args.add(remotingJar);
        
        args.add(isMaster?
            Which.jarFile(Maven3Launcher.class).getAbsolutePath():
            slaveRoot.child("maven3-interceptor.jar").getRemote());

        // TCP/IP port to establish the remoting infrastructure
        args.add(tcpPort);
        
        return args;
    }

    /**
     * Finds classworlds.jar
     */
    private static final class GetClassWorldsJar implements Callable<String,IOException> {
        private final String mvnHome;
        private final TaskListener listener;

        private GetClassWorldsJar(String mvnHome, TaskListener listener) {
            this.mvnHome = mvnHome;
            this.listener = listener;
        }

        public String call() throws IOException {
            File home = new File(mvnHome);
            if (MavenProcessFactory.debug)
                listener.getLogger().println("Using mvnHome: "+ mvnHome);
            File bootDir = new File(home, "boot");
            File[] classworlds = bootDir.listFiles(CLASSWORLDS_FILTER);
            if(classworlds==null || classworlds.length==0) {
                // Maven 2.0.6 puts it to a different place
                bootDir = new File(home, "boot");
                classworlds = bootDir.listFiles(CLASSWORLDS_FILTER);
                if(classworlds==null || classworlds.length==0) {
                    // FIXME use messages
                    //listener.error(Messages.MavenProcessFactory_ClassWorldsNotFound(home));
                    listener.error("classworld not found");
                    throw new RunnerAbortedException();
                }
            }
            return classworlds[0].getAbsolutePath();
        }
    }
    /**
     * Locates classworlds jar file.
     *
     * Note that Maven 3.0 changed the name to plexus-classworlds 
     *
     * <pre>
     * $ find tools/ -name "plexus-classworlds*.jar"
     * tools/maven-3.0-alpha-2/boot/plexus-classworlds-1.3.jar
     * tools/maven-3.0-alpha-3/boot/plexus-classworlds-2.2.2.jar
     * tools/maven-3.0-alpha-4/boot/plexus-classworlds-2.2.2.jar
     * tools/maven-3.0-alpha-5/boot/plexus-classworlds-2.2.2.jar
     * tools/maven-3.0-alpha-6/boot/plexus-classworlds-2.2.2.jar
     * </pre>
     */
    private static final FilenameFilter CLASSWORLDS_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.contains("plexus-classworlds") && name.endsWith(".jar");
        }
    };

}
