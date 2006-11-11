//========================================================================
//$Id: RunMojo.java 1078 2006-11-11 03:15:51Z kohsuke $
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================
package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.Artifact;
import org.mortbay.jetty.plugin.util.Scanner;
import org.mortbay.jetty.plugin.util.SystemProperty;
import org.mortbay.jetty.plugin.util.Scanner.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Runs Hudson with the current plugin project.
 *
 * <p>
 * This only needs the source files to be compiled, so run in the compile phase.
 * 
 * @goal run
 * @requiresDependencyResolution test
 * @execute phase=compile
 * @description Runs Hudson with the current plugin
 * @author Kohsuke Kawaguchi
 */
public class RunMojo extends AbstractJetty6Mojo {

    /**
     * The location of the war file.
     *
     * <p>
     * Normally this should be left empty, in which case the plugin loads it from the repository.
     * But this parameter allows that to be overwritten.
     *
     * @parameter
     */
    private File webApp;

    /**
     * Path to <tt>$HUDSON_HOME</tt>. The launched hudson will use this directory as the workspace.
     *
     * @parameter expression="${hudsonHome}" default-value="./work"
     */
    private File hudsonHome;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    protected File warSourceDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // look for hudson.war
        for( Artifact a : (Set<Artifact>)getProject().getArtifacts() ) {
            if(a.getArtifactId().equals("hudson-war") && a.getType().equals("war")) {
                webApp = a.getFile();
            }
        }

        if(webApp==null) {
            getLog().error(
                "Unable to locate hudson.war. Add the following dependency in your POM:\n" +
                "\n" +
                "<dependency>\n" +
                "  <groupId>org.jvnet.hudson.main</groupId>\n" +
                "  <artifactId>hudson-war</artifactId>\n" +
                "  <type>war</type>\n" +
                "  <version>1.60<!-- replace this with the version you want--></version>\n" +
                "  <scope>test</scope>\n" +
                "</dependency>"
            );
            throw new MojoExecutionException("Unable to find hudson.war");
        }

        // set HUDSON_HOME
        SystemProperty sp = new SystemProperty();
        sp.setName("HUDSON_HOME");
        sp.setValue(hudsonHome.getAbsolutePath());
        sp.setIfNotSetAlready();
        new File(hudsonHome,"plugins").mkdirs();

        // enable view auto refreshing via stapler
        sp = new SystemProperty();
        sp.setName("stapler.jelly.noCache");
        sp.setValue("true");
        sp.setIfNotSetAlready();

        generateHpl();

        super.execute();
    }

    /**
     * Create a dot-hpl file.
     *
     * <p>
     * All I want to do here is to invoke the hpl target.
     * there must be a better way to do this!
     *
     * <p>
     * Besides, if the user wants to change the plugin name, etc,
     * this forces them to do it in two places.
     */
    private void generateHpl() throws MojoExecutionException, MojoFailureException {
        HplMojo hpl = new HplMojo();
        hpl.project = getProject();
        hpl.setHudsonHome(hudsonHome);
        hpl.setLog(getLog());
        hpl.pluginName = getProject().getName();
        hpl.warSourceDirectory = warSourceDirectory;
        hpl.execute();
    }

    public void configureWebApplication() throws Exception {
        super.configureWebApplication();
        getWebApplication().setWebAppSrcDir(webApp);
    }

    public void configureScanner() throws MojoExecutionException {
        setUpScanList(new ArrayList());

        ArrayList<Listener> listeners = new ArrayList<Listener>();
        listeners.add(new Listener() {
            public void changesDetected(Scanner scanner, List changes) {
                try {
                    getLog().info("Restarting webapp ...");
                    getLog().debug("Stopping webapp ...");
                    getWebApplication().stop();
                    getLog().debug("Reconfiguring webapp ...");

                    checkPomConfiguration();

                    // check if we need to reconfigure the scanner,
                    // which is if the pom changes
                    if (changes.contains(getProject().getFile().getCanonicalPath())) {
                        getLog().info("Reconfiguring scanner after change to pom.xml ...");
                        generateHpl(); // regenerate hpl if POM changes.
                        ArrayList scanList = getScanList();
                        scanList.clear();
                        setUpScanList(scanList);
                        scanner.setRoots(scanList);
                    }

                    getLog().debug("Restarting webapp ...");
                    getWebApplication().start();
                    getLog().info("Restart completed.");
                } catch (Exception e) {
                    getLog().error("Error reconfiguring/restarting webapp after change in watched files", e);
                }
            }
        });
        setScannerListeners(listeners);

    }

    private void setUpScanList(ArrayList scanList) {
        scanList.add(getProject().getFile());
        scanList.add(webApp);
        scanList.add(new File(getProject().getBuild().getOutputDirectory()));
        setScanList(scanList);
    }

    public void checkPomConfiguration() throws MojoExecutionException {
    }

    public void finishConfigurationBeforeStart() {
    }
}
