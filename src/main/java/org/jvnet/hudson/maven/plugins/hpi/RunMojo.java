//========================================================================
//$Id: RunMojo.java 36037 2010-10-18 09:48:58Z kohsuke $
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.commons.io.FileUtils;
import org.mortbay.jetty.plugin.util.Scanner;
import org.mortbay.jetty.plugin.util.Scanner.Listener;
import org.mortbay.jetty.plugin.util.SystemProperty;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Runs Hudson with the current plugin project.
 *
 * <p>
 * This only needs the source files to be compiled, so run in the compile phase.
 * </p>
 *
 * <p>
 * To specify the HTTP port, use <tt>-Djetty.port=<i>PORT</i></tt>
 * </p>
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
     * </p>
     * 
     * @parameter
     */
    private File webApp;

    /**
     * Path to <tt>$HUDSON_HOME</tt>. The launched hudson will use this directory as the workspace.
     *
     * @parameter expression="${HUDSON_HOME}"
     */
    private File hudsonHome;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    protected File warSourceDirectory;

    /**
     * @component
     */
    protected ArtifactResolver artifactResolver;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * Specifies the HTTP port number.
     *
     * If connectors are configured in the Mojo, that'll take precedence.
     *
     * @parameter expression="${port}"
     */
    protected String defaultPort;

    /**
     * If true, the context will be restarted after a line feed on
     * the input console. Disabled by default.
     *
     * @parameter expression="${jetty.consoleForceReload}" default-value="true"
     */
    protected boolean consoleForceReload;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // compute hudsonHome
        if(hudsonHome==null) {
            String h = System.getenv("HUDSON_HOME");
            if(h!=null)
                hudsonHome = new File(h);
            else
                hudsonHome = new File("./work");
        }

        // auto-enable stapler trace, unless otherwise configured already.
        setSystemPropertyIfEmpty("stapler.trace", "true");
        // run YUI in the debug mode, unless otherwise configured
        setSystemPropertyIfEmpty("debug.YUI","true");
        // allow Jetty to accept a bigger form so that it can handle update center JSON post
        setSystemPropertyIfEmpty("org.mortbay.jetty.Request.maxFormContentSize","-1");
        // general-purpose system property so that we can tell from Hudson if we are running in the hpi:run mode.
        setSystemPropertyIfEmpty("hudson.hpi.run","true");
        // this adds 3 secs to the shutdown time. Skip it.
        setSystemPropertyIfEmpty("hudson.DNSMultiCast.disabled","true");

        List<Artifact> hudsonArtifacts = new ArrayList<Artifact>();

        // look for hudson.war
        for( Artifact a : (Set<Artifact>)getProject().getArtifacts() ) {
            if(a.getArtifactId().equals("hudson-war") && a.getType().equals("war")) {
                webApp = a.getFile();
            }
            if(a.getGroupId().equals("org.jvnet.hudson.main"))
                hudsonArtifacts.add(a);
        }

        if(webApp==null) {
            getLog().error(
                "Unable to locate hudson.war. Add the following dependency in your POM:\n" +
                "\n" +
                "<dependency>\n" +
                "  <groupId>org.jvnet.hudson.main</groupId>\n" +
                "  <artifactId>hudson-war</artifactId>\n" +
                "  <type>war</type>\n" +
                "  <version>1.293<!-- replace this with the version you want--></version>\n" +
                "  <scope>test</scope>\n" +
                "</dependency>"
            );
            throw new MojoExecutionException("Unable to find hudson.war");
        }

        // make sure all the relevant Hudson artifacts have the same version
        for (Artifact a : hudsonArtifacts) {
            Artifact ba = hudsonArtifacts.get(0);
            if(!a.getVersion().equals(ba.getVersion()))
                throw new MojoExecutionException("Version of "+a.getId()+" is inconsistent with "+ba.getId());
        }

        // set HUDSON_HOME
        SystemProperty sp = new SystemProperty();
        sp.setName("HUDSON_HOME");
        sp.setValue(hudsonHome.getAbsolutePath());
        sp.setIfNotSetAlready();
        File pluginsDir = new File(hudsonHome, "plugins");
        pluginsDir.mkdirs();

        // enable view auto refreshing via stapler
        sp = new SystemProperty();
        sp.setName("stapler.jelly.noCache");
        sp.setValue("true");
        sp.setIfNotSetAlready();

        List res = getProject().getBuild().getResources();
        if(!res.isEmpty()) {
            // pick up the first one and use it
            Resource r = (Resource) res.get(0);
            sp = new SystemProperty();
            sp.setName("stapler.resourcePath");
            sp.setValue(r.getDirectory());
            sp.setIfNotSetAlready();
        }


        generateHpl();

        // copy other dependency hudson plugins
        try {
            for( Artifact a : (Set<Artifact>)getProject().getArtifacts() ) {
                if(!HpiUtil.isPlugin(a))
                    continue;
                getLog().info("Copying dependency hudson plugin "+a.getFile());

                // find corresponding .hpi file
                Artifact hpi = artifactFactory.createArtifact(a.getGroupId(),a.getArtifactId(),a.getVersion(),null,"hpi");
                artifactResolver.resolve(hpi,getProject().getRemoteArtifactRepositories(), localRepository);

                copyFile(hpi.getFile(),new File(pluginsDir,a.getArtifactId()+".hpi"));
                // pin the dependency plugin, so that even if a different version of the same plugin is bundled to Hudson,
                // we still use the plugin as specified by the POM of the plugin.
                FileUtils.writeStringToFile(new File(pluginsDir,a.getArtifactId()+".hpi.pinned"),"pinned");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy dependency plugin",e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Unable to copy dependency plugin",e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Unable to copy dependency plugin",e);
        }

        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new MaskingClassLoader(ccl));
        try {
            super.execute();
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }

    private void setSystemPropertyIfEmpty(String name, String value) {
        if(System.getProperty(name)==null)
            System.setProperty(name, value);
    }

    private void copyFile(File src, File dst) {
        Copy cp = new Copy();
        cp.setProject(new Project());
        cp.setFile(src);
        cp.setTofile(dst);
        cp.execute();
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
        hpl.includeTestScope = true;
        hpl.execute();
    }

    public void configureWebApplication() throws Exception {
        // Jetty tries to do this in WebAppContext.resolveWebApp but it failed to delete the directory.
        File extractedWebAppDir= new File(getTmpDirectory(), "webapp");
        if(extractedWebAppDir.lastModified() < webApp.lastModified())
            FileUtils.deleteDirectory(extractedWebAppDir);
        
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

    @Override
    protected void startScanner() {
        super.startScanner();

        if (consoleForceReload) {
            getLog().info("Console reloading is ENABLED. Hit ENTER on the console to restart the context.");
            new ConsoleScanner(this).start();
        }
    }

    public void checkPomConfiguration() throws MojoExecutionException {
    }

    public void finishConfigurationBeforeStart() {
        // working around JETTY-1226. This bug affects those who use Axis from plugins, for example.
        WebAppContext wac = (WebAppContext)getWebApplication().getProxiedObject();
        List<String> sc = new ArrayList<String>(Arrays.asList(wac.getSystemClasses()));
        sc.add("javax.activation.");
        wac.setSystemClasses(sc.toArray(new String[sc.size()]));
    }

    @Override
    protected String getDefaultHttpPort() {
        if (defaultPort!=null)
            return defaultPort;
        return super.getDefaultHttpPort();
    }
}
