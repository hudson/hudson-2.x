/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Jean-Baptiste Quenot, Tom Huybrechts
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson;

import hudson.PluginWrapper.Dependency;
import hudson.model.Hudson;
import hudson.util.IOException2;
import hudson.util.MaskingClassLoader;
import hudson.util.VersionNumber;
import hudson.Plugin.DummyImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;

public class ClassicPluginStrategy implements PluginStrategy {

    private static final Logger LOGGER = Logger.getLogger(ClassicPluginStrategy.class.getName());

    /**
     * Filter for jar files.
     */
    private static final FilenameFilter JAR_FILTER = new FilenameFilter() {
        public boolean accept(File dir,String name) {
            return name.endsWith(".jar");
        }
    };

    private PluginManager pluginManager;

    public ClassicPluginStrategy(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public PluginWrapper createPluginWrapper(File archive) throws IOException {
        final Manifest manifest;
        URL baseResourceURL;

        File expandDir = null;
        // if .hpi, this is the directory where war is expanded

        boolean isLinked = archive.getName().endsWith(".hpl");
        if (isLinked) {
            // resolve the .hpl file to the location of the manifest file
            String firstLine = new BufferedReader(new FileReader(archive))
                    .readLine();
            if (firstLine.startsWith("Manifest-Version:")) {
                // this is the manifest already
            } else {
                // indirection
                archive = resolve(archive, firstLine);
            }
            // then parse manifest
            FileInputStream in = new FileInputStream(archive);
            try {
                manifest = new Manifest(in);
            } catch (IOException e) {
                throw new IOException2("Failed to load " + archive, e);
            } finally {
                in.close();
            }
        } else {
            if (archive.isDirectory()) {// already expanded
                expandDir = archive;
            } else {
                expandDir = new File(archive.getParentFile(), PluginWrapper.getBaseName(archive));
                explode(archive, expandDir);
            }

            File manifestFile = new File(expandDir, "META-INF/MANIFEST.MF");
            if (!manifestFile.exists()) {
                throw new IOException(
                        "Plugin installation failed. No manifest at "
                                + manifestFile);
            }
            FileInputStream fin = new FileInputStream(manifestFile);
            try {
                manifest = new Manifest(fin);
            } finally {
                fin.close();
            }
        }

        final Attributes atts = manifest.getMainAttributes();

        // TODO: define a mechanism to hide classes
        // String export = manifest.getMainAttributes().getValue("Export");

        List<File> paths = new ArrayList<File>();
        if (isLinked) {
            parseClassPath(manifest, archive, paths, "Libraries", ",");
            parseClassPath(manifest, archive, paths, "Class-Path", " +"); // backward compatibility

            baseResourceURL = resolve(archive,atts.getValue("Resource-Path")).toURI().toURL();
        } else {
            File classes = new File(expandDir, "WEB-INF/classes");
            if (classes.exists())
                paths.add(classes);
            File lib = new File(expandDir, "WEB-INF/lib");
            File[] libs = lib.listFiles(JAR_FILTER);
            if (libs != null)
                paths.addAll(Arrays.asList(libs));

            baseResourceURL = expandDir.toURI().toURL();
        }
        File disableFile = new File(archive.getPath() + ".disabled");
        if (disableFile.exists()) {
            LOGGER.info("Plugin " + archive.getName() + " is disabled");
        }

        // compute dependencies
        List<PluginWrapper.Dependency> dependencies = new ArrayList<PluginWrapper.Dependency>();
        List<PluginWrapper.Dependency> optionalDependencies = new ArrayList<PluginWrapper.Dependency>();
        String v = atts.getValue("Plugin-Dependencies");
        if (v != null) {
            for (String s : v.split(",")) {
                PluginWrapper.Dependency d = new PluginWrapper.Dependency(s);
                if (d.optional) {
                    optionalDependencies.add(d);
                } else {
                    dependencies.add(d);
                }
            }
        }
        for (DetachedPlugin detached : DETACHED_LIST)
            detached.fix(atts,optionalDependencies);

        ClassLoader dependencyLoader = new DependencyClassLoader(getBaseClassLoader(atts), archive, Util.join(dependencies,optionalDependencies));

        return new PluginWrapper(pluginManager, archive, manifest, baseResourceURL,
                createClassLoader(paths, dependencyLoader, atts), disableFile, dependencies, optionalDependencies);
    }
    
    @Deprecated
    protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent) throws IOException {
        return createClassLoader( paths, parent, null );
    }

    /**
     * Creates the classloader that can load all the specified jar files and delegate to the given parent.
     */
    protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent, Attributes atts) throws IOException {
        if (atts != null) {
            String usePluginFirstClassLoader = atts.getValue( "PluginFirstClassLoader" );
            if (Boolean.valueOf( usePluginFirstClassLoader )) {
                PluginFirstClassLoader classLoader = new PluginFirstClassLoader();
                classLoader.setParentFirst( false );
                classLoader.setParent( parent );
                classLoader.addPathFiles( paths );
                return classLoader;
            }
        }
        if(useAntClassLoader) {
            // using AntClassLoader with Closeable so that we can predictably release jar files opened by URLClassLoader
            AntClassLoader2 classLoader = new AntClassLoader2(parent);
            classLoader.addPathFiles(paths);
            return classLoader;
        } else {
            // Tom reported that AntClassLoader has a performance issue when Hudson keeps trying to load a class that doesn't exist,
            // so providing a legacy URLClassLoader support, too
            List<URL> urls = new ArrayList<URL>();
            for (File path : paths)
                urls.add(path.toURI().toURL());
            return new URLClassLoader(urls.toArray(new URL[urls.size()]),parent);
        }
    }

    /**
     * Information about plugins that were originally in the core.
     */
    private static final class DetachedPlugin {
        private final String shortName;
        private final VersionNumber splitWhen;
        private final String requireVersion;

        private DetachedPlugin(String shortName, String splitWhen, String requireVersion) {
            this.shortName = shortName;
            this.splitWhen = new VersionNumber(splitWhen);
            this.requireVersion = requireVersion;
        }

        private void fix(Attributes atts, List<PluginWrapper.Dependency> optionalDependencies) {
            // don't fix the dependency for yourself, or else we'll have a cycle
            String yourName = atts.getValue("Short-Name");
            if (shortName.equals(yourName))   return;

            // some earlier versions of maven-hpi-plugin apparently puts "null" as a literal in Hudson-Version. watch out for them.
            String hudsonVersion = atts.getValue("Hudson-Version");
            if (hudsonVersion == null || hudsonVersion.equals("null") || new VersionNumber(hudsonVersion).compareTo(splitWhen) <= 0)
                optionalDependencies.add(new PluginWrapper.Dependency(shortName+':'+requireVersion));
        }
    }

    private static final List<DetachedPlugin> DETACHED_LIST = Arrays.asList(
        new DetachedPlugin("maven-plugin","1.296","1.296"),
        new DetachedPlugin("subversion","1.310","1.0"),
        new DetachedPlugin("cvs","1.340","0.1")
    );

    /**
     * Computes the classloader that takes the class masking into account.
     *
     * <p>
     * This mechanism allows plugins to have their own verions for libraries that core bundles.
     */
    private ClassLoader getBaseClassLoader(Attributes atts) {
        ClassLoader base = getClass().getClassLoader();
        String masked = atts.getValue("Mask-Classes");
        if(masked!=null)
            base = new MaskingClassLoader(base, masked.trim().split("[ \t\r\n]+"));
        return base;
    }

    public void initializeComponents(PluginWrapper plugin) {
    }

    public <T> List<ExtensionComponent<T>> findComponents(Class<T> type, Hudson hudson) {

        List<ExtensionFinder> finders;
        if (type==ExtensionFinder.class) {
            // Avoid infinite recursion of using ExtensionFinders to find ExtensionFinders
            finders = Collections.<ExtensionFinder>singletonList(new ExtensionFinder.Sezpoz());
        } else {
            finders = hudson.getExtensionList(ExtensionFinder.class);
        }

        /**
         * See {@link ExtensionFinder#scout(Class, Hudson)} for the dead lock issue and what this does.
         */
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.log(Level.FINER,"Scout-loading ExtensionList: "+type, new Throwable());
        for (ExtensionFinder finder : finders) {
            finder.scout(type, hudson);
        }

        List<ExtensionComponent<T>> r = new ArrayList<ExtensionComponent<T>>();
        for (ExtensionFinder finder : finders) {
            try {
                r.addAll(finder._find(type, hudson));
            } catch (AbstractMethodError e) {
                // backward compatibility
                for (T t : finder.findExtensions(type, hudson))
                    r.add(new ExtensionComponent<T>(t));
            }
        }
        return r;
    }

    public void load(PluginWrapper wrapper) throws IOException {
        // override the context classloader so that XStream activity in plugin.start()
        // will be able to resolve classes in this plugin
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(wrapper.classLoader);
        try {
            String className = wrapper.getPluginClass();
            if(className==null) {
                // use the default dummy instance
                wrapper.setPlugin(new DummyImpl());
            } else {
                try {
                    Class clazz = wrapper.classLoader.loadClass(className);
                    Object o = clazz.newInstance();
                    if(!(o instanceof Plugin)) {
                        throw new IOException(className+" doesn't extend from hudson.Plugin");
                    }
                    wrapper.setPlugin((Plugin) o);
                } catch (LinkageError e) {
                    throw new IOException2("Unable to load " + className + " from " + wrapper.getShortName(),e);
                } catch (ClassNotFoundException e) {
                    throw new IOException2("Unable to load " + className + " from " + wrapper.getShortName(),e);
                } catch (IllegalAccessException e) {
                    throw new IOException2("Unable to create instance of " + className + " from " + wrapper.getShortName(),e);
                } catch (InstantiationException e) {
                    throw new IOException2("Unable to create instance of " + className + " from " + wrapper.getShortName(),e);
                }
            }

            // initialize plugin
            try {
                Plugin plugin = wrapper.getPlugin();
                plugin.setServletContext(pluginManager.context);
                startPlugin(wrapper);
            } catch(Throwable t) {
                // gracefully handle any error in plugin.
                throw new IOException2("Failed to initialize",t);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void startPlugin(PluginWrapper plugin) throws Exception {
        plugin.getPlugin().start();
    }

    private static File resolve(File base, String relative) {
        File rel = new File(relative);
        if(rel.isAbsolute())
            return rel;
        else
            return new File(base.getParentFile(),relative);
    }

    private static void parseClassPath(Manifest manifest, File archive, List<File> paths, String attributeName, String separator) throws IOException {
        String classPath = manifest.getMainAttributes().getValue(attributeName);
        if(classPath==null) return; // attribute not found
        for (String s : classPath.split(separator)) {
            File file = resolve(archive, s);
            if(file.getName().contains("*")) {
                // handle wildcard
                FileSet fs = new FileSet();
                File dir = file.getParentFile();
                fs.setDir(dir);
                fs.setIncludes(file.getName());
                for( String included : fs.getDirectoryScanner(new Project()).getIncludedFiles() ) {
                    paths.add(new File(dir,included));
                }
            } else {
                if(!file.exists())
                    throw new IOException("No such file: "+file);
                paths.add(file);
            }
        }
    }

    /**
     * Explodes the plugin into a directory, if necessary.
     */
    private static void explode(File archive, File destDir) throws IOException {
        if(!destDir.exists())
            destDir.mkdirs();

        // timestamp check
        File explodeTime = new File(destDir,".timestamp");
        if(explodeTime.exists() && explodeTime.lastModified()==archive.lastModified())
            return; // no need to expand

        // delete the contents so that old files won't interfere with new files
        Util.deleteContentsRecursive(destDir);

        try {
            Expand e = new Expand();
            e.setProject(new Project());
            e.setTaskType("unzip");
            e.setSrc(archive);
            e.setDest(destDir);
            e.execute();
        } catch (BuildException x) {
            throw new IOException2("Failed to expand " + archive,x);
        }

        try {
            new FilePath(explodeTime).touch(archive.lastModified());
        } catch (InterruptedException e) {
            throw new AssertionError(e); // impossible
        }
    }

    /**
     * Used to load classes from dependency plugins.
     */
    final class DependencyClassLoader extends ClassLoader {
        /**
         * This classloader is created for this plugin. Useful during debugging.
         */
        private final File _for;

        private List<Dependency> dependencies;

        public DependencyClassLoader(ClassLoader parent, File archive, List<Dependency> dependencies) {
            super(parent);
            this._for = archive;
            this.dependencies = dependencies;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            for (Dependency dep : dependencies) {
                PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                if(p!=null)
                    try {
                        return p.classLoader.loadClass(name);
                    } catch (ClassNotFoundException _) {
                        // try next
                    }
            }

            throw new ClassNotFoundException(name);
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            HashSet<URL> result = new HashSet<URL>();
            for (Dependency dep : dependencies) {
                PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                if (p!=null) {
                    Enumeration<URL> urls = p.classLoader.getResources(name);
                    while (urls != null && urls.hasMoreElements())
                        result.add(urls.nextElement());
                }
            }

            return Collections.enumeration(result);
        }

        @Override
        protected URL findResource(String name) {
            for (Dependency dep : dependencies) {
                PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                if(p!=null) {
                    URL url = p.classLoader.getResource(name);
                    if (url!=null)
                        return url;
                }
            }

            return null;
        }
    }

    /**
     * {@link AntClassLoader} with a few methods exposed and {@link Closeable} support.
     */
    private static final class AntClassLoader2 extends AntClassLoader implements Closeable {
        private AntClassLoader2(ClassLoader parent) {
            super(parent,true);
        }

        public void addPathFiles(Collection<File> paths) throws IOException {
            for (File f : paths)
                addPathFile(f);
        }

        public void close() throws IOException {
            cleanup();
        }
    }

    public static boolean useAntClassLoader = Boolean.getBoolean(ClassicPluginStrategy.class.getName()+".useAntClassLoader");
}
