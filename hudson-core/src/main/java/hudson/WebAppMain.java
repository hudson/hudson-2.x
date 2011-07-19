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
*    Kohsuke Kawaguchi, Jean-Baptiste Quenot, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson;

import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.stapler.WebAppController;
import hudson.stapler.WebAppController.DefaultInstallStrategy;
import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;
import hudson.util.HudsonIsLoading;
import hudson.util.IncompatibleServletVersionDetected;
import hudson.util.IncompatibleVMDetected;
import hudson.util.InsufficientPermissionDetected;
import hudson.util.NoHomeDir;
import hudson.util.RingBufferLogHandler;
import hudson.util.NoTempDir;
import hudson.util.IncompatibleAntVersionDetected;
import hudson.util.HudsonFailedToLoad;
import hudson.util.AWTProblem;
import hudson.util.graph.ChartUtil;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.apache.tools.ant.types.FileSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletResponse;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.Security;

/**
 * Entry point when Hudson is used as a webapp.
 *
 * @author Kohsuke Kawaguchi
 */
public final class WebAppMain implements ServletContextListener {
    private final RingBufferLogHandler handler = new RingBufferLogHandler();

    /**
     * Creates the sole instance of {@link Hudson} and register it to the {@link ServletContext}.
     */
    public void contextInitialized(ServletContextEvent event) {
        try {
            final ServletContext context = event.getServletContext();

            // Install the current servlet context, unless its already been set
            final WebAppController controller = WebAppController.get();
            try {
                // Attempt to set the context
                controller.setContext(context);
            }
            catch (IllegalStateException e) {
                // context already set ignore
            }

            // Setup the default install strategy if not already configured
            try {
                controller.setInstallStrategy(new DefaultInstallStrategy());
            }
            catch (IllegalStateException e) {
                // strategy already set ignore
            }

            // use the current request to determine the language
            LocaleProvider.setProvider(new LocaleProvider() {
                public Locale get() {
                    Locale locale=null;
                    StaplerRequest req = Stapler.getCurrentRequest();
                    if(req!=null)
                        locale = req.getLocale();
                    if(locale==null)
                        locale = Locale.getDefault();
                    return locale;
                }
            });

            // quick check to see if we (seem to) have enough permissions to run. (see #719)
            JVM jvm;
            try {
                jvm = new JVM();
                new URLClassLoader(new URL[0],getClass().getClassLoader());
            } catch(SecurityException e) {
                controller.install(new InsufficientPermissionDetected(e));
                return;
            }

            try {// remove Sun PKCS11 provider if present. See http://wiki.hudson-ci.org/display/HUDSON/Solaris+Issue+6276483
                Security.removeProvider("SunPKCS11-Solaris");
            } catch (SecurityException e) {
                // ignore this error.
            }

            installLogger();

            File dir = getHomeDir(event);
            try {
                dir = dir.getCanonicalFile();
            }
            catch (IOException e) {
                dir = dir.getAbsoluteFile();
            }
            final File home = dir;
            home.mkdirs();

            LOGGER.info("Home directory: " + home);

            // check that home exists (as mkdirs could have failed silently), otherwise throw a meaningful error
            if (! home.exists()) {
                controller.install(new NoHomeDir(home));
                return;
            }

            // make sure that we are using XStream in the "enhanced" (JVM-specific) mode
            if(jvm.bestReflectionProvider().getClass()==PureJavaReflectionProvider.class) {
                // nope
                controller.install(new IncompatibleVMDetected());
                return;
            }

//  JNA is no longer a hard requirement. It's just nice to have. See HUDSON-4820 for more context.
//            // make sure JNA works. this can fail if
//            //    - platform is unsupported
//            //    - JNA is already loaded in another classloader
//            // see http://wiki.hudson-ci.org/display/HUDSON/JNA+is+already+loaded
//            // TODO: or shall we instead modify Hudson to work gracefully without JNA?
//            try {
//                /*
//                    java.lang.UnsatisfiedLinkError: Native Library /builds/apps/glassfish/domains/hudson-domain/generated/jsp/j2ee-modules/hudson-1.309/loader/com/sun/jna/sunos-sparc/libjnidispatch.so already loaded in another classloader
//                        at java.lang.ClassLoader.loadLibrary0(ClassLoader.java:1743)
//                        at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1674)
//                        at java.lang.Runtime.load0(Runtime.java:770)
//                        at java.lang.System.load(System.java:1005)
//                        at com.sun.jna.Native.loadNativeLibraryFromJar(Native.java:746)
//                        at com.sun.jna.Native.loadNativeLibrary(Native.java:680)
//                        at com.sun.jna.Native.<clinit>(Native.java:108)
//                        at hudson.util.jna.GNUCLibrary.<clinit>(GNUCLibrary.java:86)
//                        at hudson.Util.createSymlink(Util.java:970)
//                        at hudson.model.Run.run(Run.java:1174)
//                        at hudson.matrix.MatrixBuild.run(MatrixBuild.java:149)
//                        at hudson.model.ResourceController.execute(ResourceController.java:88)
//                        at hudson.model.Executor.run(Executor.java:123)
//                 */
//                String.valueOf(Native.POINTER_SIZE); // this meaningless operation forces the classloading and initialization
//            } catch (LinkageError e) {
//                if (e.getMessage().contains("another classloader"))
//                    controller.install(new JNADoublyLoaded(e));
//                else
//                    controller.install(new HudsonFailedToLoad(e));
//            }

            // make sure this is servlet 2.4 container or above
            try {
                ServletResponse.class.getMethod("setCharacterEncoding",String.class);
            } catch (NoSuchMethodException e) {
                controller.install(new IncompatibleServletVersionDetected(ServletResponse.class));
                return;
            }

            // make sure that we see Ant 1.7
            try {
                FileSet.class.getMethod("getDirectoryScanner");
            } catch (NoSuchMethodException e) {
                controller.install(new IncompatibleAntVersionDetected(FileSet.class));
                return;
            }

            //make sure AWT is functioning, or else JFreeChart won't even load.
            if(ChartUtil.awtProblemCause!=null) {
                controller.install(new AWTProblem(ChartUtil.awtProblemCause));
                return;
            }

            // some containers (in particular Tomcat) doesn't abort a launch
            // even if the temp directory doesn't exist.
            // check that and report an error
            try {
                File f = File.createTempFile("test", "test");
                f.delete();
            } catch (IOException e) {
                controller.install(new NoTempDir(e));
                return;
            }

            // Tomcat breaks XSLT with JDK 5.0 and onward. Check if that's the case, and if so,
            // try to correct it
            try {
                TransformerFactory.newInstance();
                // if this works we are all happy
            } catch (TransformerFactoryConfigurationError x) {
                // no it didn't.
                LOGGER.log(Level.WARNING, "XSLT not configured correctly. Hudson will try to fix this. See http://issues.apache.org/bugzilla/show_bug.cgi?id=40895 for more details",x);
                System.setProperty(TransformerFactory.class.getName(),"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
                try {
                    TransformerFactory.newInstance();
                    LOGGER.info("XSLT is set to the JAXP RI in JRE");
                } catch(TransformerFactoryConfigurationError y) {
                    LOGGER.log(Level.SEVERE, "Failed to correct the problem.");
                }
            }

            installExpressionFactory(event);

            controller.install(new HudsonIsLoading());

            new Thread("hudson initialization thread") {
                @Override
                public void run() {
                    try {
                        // Creating of the god object performs most of the booting muck
                        Hudson hudson = new Hudson(home,context);

                        // once its done, hook up to stapler and things should be ready to go
                        controller.install(hudson);

                        // trigger the loading of changelogs in the background,
                        // but give the system 10 seconds so that the first page
                        // can be served quickly
                        Trigger.timer.schedule(new SafeTimerTask() {
                            public void doRun() {
                                User.getUnknown().getBuilds();
                            }
                        }, 1000*10);
                    } catch (Error e) {
                        LOGGER.log(Level.SEVERE, "Failed to initialize Hudson",e);
                        controller.install(new HudsonFailedToLoad(e));
                        throw e;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to initialize Hudson",e);
                        controller.install(new HudsonFailedToLoad(e));
                    }
                }
            }.start();
        } catch (Error e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Hudson",e);
            throw e;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Hudson",e);
            throw e;
        }
    }

    public static void installExpressionFactory(ServletContextEvent event) {
        JellyFacet.setExpressionFactory(event, new ExpressionFactory2());
    }

	/**
     * Installs log handler to monitor all Hudson logs.
     */
    private void installLogger() {
        Hudson.logRecords = handler.getView();
        Logger.getLogger("hudson").addHandler(handler);
    }

    /**
     * Determines the home directory for Hudson.
     *
     * People makes configuration mistakes, so we are trying to be nice
     * with those by doing {@link String#trim()}.
     */
    private File getHomeDir(ServletContextEvent event) {
        // check JNDI for the home directory first
        try {
            InitialContext iniCtxt = new InitialContext();
            Context env = (Context) iniCtxt.lookup("java:comp/env");
            String value = (String) env.lookup("HUDSON_HOME");
            if(value!=null && value.trim().length()>0)
                return new File(value.trim());
            // look at one more place. See issue #1314 
            value = (String) iniCtxt.lookup("HUDSON_HOME");
            if(value!=null && value.trim().length()>0)
                return new File(value.trim());
        } catch (NamingException e) {
            // ignore
        }

        // finally check the system property
        String sysProp = System.getProperty("HUDSON_HOME");
        if(sysProp!=null)
            return new File(sysProp.trim());
        
        // look at the env var next
        String env = EnvVars.masterEnvVars.get("HUDSON_HOME");
        if(env!=null)
            return new File(env.trim()).getAbsoluteFile();

        // otherwise pick a place by ourselves

        String root = event.getServletContext().getRealPath("/WEB-INF/workspace");
        if(root!=null) {
            File ws = new File(root.trim());
            if(ws.exists())
                // Hudson <1.42 used to prefer this before ~/.hudson, so
                // check the existence and if it's there, use it.
                // otherwise if this is a new installation, prefer ~/.hudson
                return ws;
        }

        // if for some reason we can't put it within the webapp, use home directory.
        return new File(new File(System.getProperty("user.home")),".hudson");
    }

    public void contextDestroyed(ServletContextEvent event) {
        Hudson instance = Hudson.getInstance();
        if(instance!=null)
            instance.cleanUp();

        // Logger is in the system classloader, so if we don't do this
        // the whole web app will never be undepoyed.
        Logger.getLogger("hudson").removeHandler(handler);
    }

    private static final Logger LOGGER = Logger.getLogger(WebAppMain.class.getName());

}
