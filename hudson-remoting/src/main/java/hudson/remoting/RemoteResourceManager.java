/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation Inc., Kohsuke Kawaguchi, Nikita Levyankov
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
package hudson.remoting;

import hudson.remoting.RemoteClassLoader.IClassLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.io.IOUtils;

/**
 * This class is for managing local resources that are sent remotely to a RemoteClassLoader
 * instance. It handles the case of individual resources as while as entire JAR files that can
 * optionally be preloaded in their entirety for performance reasons.
 */
public class RemoteResourceManager {

    /**
     * The temp folder for creating local resources that were downloaded from the remote class
     * loader. This is set with package private accessibility for the unit test.
     */
    static final String TEMP_FOLDER_NAME = "hudson-remoting";

    /** The name of the protocol to use when creating JAR URLs. */
    private final static String JAR_URL_PROTOCOL = "jar";

    /**
     * The proxy instance that will be used for looking up resources and downloading them as
     * necessary.
     */
    private IClassLoader proxyClassLoader;

    /**
     * The channel used to communicate between remote node and server. This is primarily needed just
     * to verify whether the channel allows remote loading of clases (i.e. whether it is not
     * restricted).
     */
    private Channel channel;

    /**
     * The remote class loader instance that is needed so that prefetched JARs can be loaded into
     * it.
     */
    private RemoteClassLoader remoteClassLoader;

    /**
     * The list of prefetched JARs. The String is the externalized form of the JAR's remote URL and
     * the File is the path to the local JAR that was downloaded from the proxy. Note that the
     * externalized form of the remote URL is also decoded of any hex codes (e.g. %20 for space
     * character).
     */
    Map<String, File> prefetchedJars = new HashMap<String, File>();

    /**
     * The list of resources that have been found so far and have been downloaded from the proxy and
     * stored locally. The String is the externalized form of the JAR's remote URL and the File is
     * the path to the local resource that was downloaded from the proxy. Note that the externalized
     * form of the remote URL is also decoded of any hex codes (e.g. %20 for space character).
     */
    Map<String, File> resourcesMap = new HashMap<String, File>();

    /**
     * The list of resources that have been founds so far that have been downloaded from the proxy
     * and stored locally when findResource() is called. The String is the name that was looked up
     * and the File is the path to the local resources that was downloaded from the proxy.
     */
    Map<String, File> resourceMap = new HashMap<String, File>();

    /**
     * Creates and instance of RemoteResourceManager.
     *
     * @param remoteClassLoader
     *            The remote class loader instance that will be used for loading prefetched JARs.
     * @param proxyClassLoader
     *            The class loader proxy used to retrieve resources from the remote class loader.
     * @param channel
     *            The communications channel used to communicate between remote node and server.
     *            This is primarily needed just to verify whether the channel allows remote loading
     *            of classes (i.e. whether it is not restricted).
     */
    public RemoteResourceManager(RemoteClassLoader remoteClassLoader,
            IClassLoader proxyClassLoader, Channel channel) {
        this.remoteClassLoader = remoteClassLoader;
        this.proxyClassLoader = proxyClassLoader;
        this.channel = channel;
    }

    /**
     * Finds a resource by using the remote class loader's URL and the resource name. The remote
     * class loader's URL is needed as a key to determine whether the URL is already stored locally
     *
     * @param remoteUrl
     *            The File URL of the resource on the server (e.g.
     *            jar:file:/some/folder/important.jar?/images/cat.png).
     * @param name
     *            The name of the resource
     * @return Returns a local JAR URL to the resource
     * @throws IOException
     *             When some kind of issue occurs downloading the resource or storing it locally.
     */
    private URL findResource(URL remoteUrl, String name) throws IOException {
        // Check whether resource is in one of the prefetched JARs.
        if (prefetchedJars.containsKey(prefetchedJarKey(remoteUrl))) {
            return getLocalPrefetchedUrl(remoteUrl, remoteUrl.getQuery());
        }

        // Check whether resource has already been downloaded.
        File localResource = resourcesMap.get(resourcesMapKey(remoteUrl));
        if (localResource != null && localResource.exists()) {
            return toURL(localResource);
        }

        InputStream in = proxyClassLoader.getResourceAsStream(remoteUrl);

        localResource = makeResource(remoteUrl.getQuery() == null ? name : remoteUrl.getQuery(), in);
        if (!localResource.exists()) {
            return null;
        }
        resourcesMap.put(resourcesMapKey(remoteUrl), localResource);
        return toURL(localResource);
    }

    /**
     * Finds the resource with the specified name by examining the local prefetched JARs, resources
     * already downloaded from the remote classloader, and, failing those, asks the remote class
     * loader for the resource.
     *
     * @param name
     *            The resource name
     * @return A local JAR URL to the resource.
     */
    public URL findResource(String name) {
        // First attempt to load from locally fetched jars
        URL url = remoteClassLoader.findResourceInURLs(name);
        if (url != null || channel.isRestricted)
            return url;

        try {
            if (resourceMap.containsKey(name)) {
                File f = resourceMap.get(name);
                if (f == null) {
                    return null; // no such resource
                }

                if (f.exists()) {
                    // Be defensive against external factors that might have
                    // deleted this file, since we use /tmp. See
                    // http://www.nabble.com/Surefire-reports-tt17554215.html
                    return f.toURI().toURL();
                }
            }

            InputStream in = proxyClassLoader.getResourceAsStream(name);

            if (in == null) {
                resourceMap.put(name, null);
                return null;
            }

            File res = makeResource(name, in);
            resourceMap.put(name, res);
            return res.toURI().toURL();
        } catch (IOException e) {
            throw new Error("Unable to load resource " + name, e);
        }
    }

    /**
     * Returns an enumeration of URLs representing all of the resources having the specified name.
     * It finds the resources by examining the local prefetched JARs, resources already downloaded
     * from the remote classloader, and, failing those, asks the remote class loader for the
     * resources.
     * <br/>
     * The strategy used is to first ask the remote class loader for all URLs containing the
     * resource name. Then, the remote URLs are used for determining which, if any, of those URLs
     * are prefetched. If so, a URL to the local prefetched JAR is used. If not, the individual
     * resource is downloaded from the remote class loader and stored individually.
     *
     * @param name
     *            The resource name
     * @return An enumeration of local JAR URLs of the resources found.
     * @throws IOException
     *             When some kind of issue occurs downloading the resource or storing it locally.
     */
    public Enumeration<URL> findResources(String name) throws IOException {
        if (channel.isRestricted)
            return new Vector<URL>().elements();

        Vector<URL> urls = new Vector<URL>();

        Vector<URL> resources = proxyClassLoader.findResources(name);

        for (int i = 0; resources != null && i < resources.size(); i++) {
            URL curr = resources.get(i);
            URL localUrl = findResource(curr, name);
            if (localUrl != null) {
                urls.add(localUrl);
            }
        }

        return urls.elements();
    }

    /**
     * Prefetches the jar into this class loader.
     *
     * @param remoteUrl
     *            Jar to be prefetched. Note that this file is an file on the other end, and doesn't
     *            point to anything meaningful locally. The URL should be a file URL to the file
     *            on the other end (e.g. file:/some/resource.jar).
     * @return true if the prefetch happened. false if the jar is already prefetched.
     * @throws IOException if any.
     * @see Channel#preloadJar(Callable, Class[])
     */
    public boolean prefetchJar(URL remoteUrl) throws IOException {

        synchronized (prefetchedJars) {
            File prefetchedJar = prefetchedJars.get(prefetchedJarKey(remoteUrl));

            // Verify whether the JAR is prefetched and if so, that it wasn't
            // deleted. Since the file is in the temp folder, it is possible
            // that it could have been deleted by some other process.
            if (prefetchedJar != null && prefetchedJar.exists()) {
                return false;
            }

            File file = new File(Which.decode(remoteUrl.getPath()));
            String localPath = file.getName();
            File localJar = makeResource(localPath, proxyClassLoader.getJarAsStream(remoteUrl));
            remoteClassLoader.addURL(toJarURL(localJar));
            prefetchedJars.put(prefetchedJarKey(remoteUrl), localJar);
            return true;
        }
    }

    /**
     * Retrieves a resources from the remote class loader.
     *
     * @param name
     *            The name of the resource
     * @param in
     *            An input stream on the remote server for downloading the resource.
     * @return A file to the locally downloaded copy of the resource.
     * @throws IOException
     *             When some issue occurs reading the resource from the stream or creating the file
     *             locally.
     */
    private File makeResource(String name, InputStream in) throws IOException {
        File tmpFile = createTempDir();
        File resource = new File(tmpFile, name);
        resource.getParentFile().mkdirs();

        // Copy over network in a large chunk. We want to avoid copying the
        // entire resource at once into memory to avoid out of memory errors,
        // hence the use of the stram. At the same time, we still want to be
        // able to copy the data quickly.
        BufferedInputStream bufin = new BufferedInputStream(in);
        FileOutputStream fos = null;
        BufferedOutputStream bufout = null;
        try {
            fos = new FileOutputStream(resource);
            bufout = new BufferedOutputStream(fos);
            int ch;

            long startTime = System.nanoTime();
            while ((ch = bufin.read()) != -1) {
                bufout.write(ch);
            }
            channel.resourceLoadingTime.addAndGet(System.nanoTime() - startTime);
            channel.resourceLoadingCount.incrementAndGet();
        } finally {
            IOUtils.closeQuietly(bufin);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufout);
            IOUtils.closeQuietly(fos);
        }

        deleteDirectoryOnExit(tmpFile);

        return resource;
    }

    /**
     * Creates a temporary directory used for stored resources fetched from the remote class loader.
     * @return The temporary directory
     * @throws IOException When some issue occurs creating the folder. Note that due to Sun bug
     * 6325169, the method will re-try 100 times when an IOException occurs. If unsuccessful after
     * the 100 tries, the exception will be thrown.
     */
    private File createTempDir() throws IOException {
    	// work around sun bug 6325169 on windows
    	// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6325169
        int nRetry=0;
        while (true) {
            try {
                File tmpFile = File.createTempFile(TEMP_FOLDER_NAME, "");
                tmpFile.delete();
                tmpFile.mkdir();
                return tmpFile;
            } catch (IOException e) {
                if (nRetry++ < 100)
                    continue;
                throw e;
            }
        }
    }

    // FIXME move to utils
    /**
     * Instructs Java to recursively delete the given directory (dir) and its contents when the JVM
     * exits. Note that JARs loaded into a URLClassLoader may not be deleted. See:
     * http://developer.java.sun.com/developer/bugParade/bugs/4950148.html
     *
     * and, for a possible workaround see: http://www.devx.com/Java/Article/22018/1954
     *
     * @param dir
     *            File customer representing directory to delete. If this file argument is not a
     *            directory, it will still be deleted.
     *            <p>
     *            The method works in Java 1.3, Java 1.4, Java 5.0 and Java 6.0; but it does not
     *            work with some early Java 6.0 versions See
     *            http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6437591
     */
    public static void deleteDirectoryOnExit(final File dir) {
        // Delete this on exit. Delete on exit requests are processed in REVERSE
        // order
        dir.deleteOnExit();

        // If it's a directory, visit its children. This recursive walk has to
        // be done AFTER calling deleteOnExit on the directory itself because
        // Java deletes the files to be deleted on exit in reverse order.
        if (dir.isDirectory()) {
            File[] childFiles = dir.listFiles();
            if (childFiles != null) { // listFiles may return null if there's an
                // IO error
                for (File f : childFiles) {
                    deleteDirectoryOnExit(f);
                }
            }
        }
    }

    /**
     * Converts a File to a file URL.
     *
     * @param localResource
     *            The file
     * @return A file URL representing the local file.
     * @throws MalformedURLException
     *             When an issue occurs converting the file to its corresponding URL path.
     */
    private static URL toURL(File localResource) throws MalformedURLException {
        if (!localResource.exists()) {
            return null; // Abort as nothing can be done.
        }
        return localResource.toURI().toURL();
    }

    /**
     * Converts a file into a JAR URL.
     *
     * @param localResource
     *            The file
     * @return A JAR URL to the file. For example, /some/folder/important.jar would be converted to
     *         jar:file:/some/folder/important.jar!/
     * @throws MalformedURLException
     *             When an issue occurs creating the URL instance representing the JAR URL.
     */
    /*package*/ static URL toJarURL(File localResource) throws MalformedURLException {
        return toJarURL(localResource, "");
    }

    /**
     * Converts a file URL into a JAR URL appending the resource name onto the URL.
     *
     * @param localResource
     *            The URL to the file
     * @param resourceName
     *            the resource path to place into the JAR URL.
     * @return A JAR URL to the file. For example, /some/folder/important.jar with resource name of
     *         images/cat.png would be converted to
     *         jar:file:/some/folder/important.jar!/images/cat.png.
     * @throws MalformedURLException
     *             When an issue occurs creating the URL instance representing the JAR URL.
     */
    private static URL toJarURL(File localResource, String resourceName) throws MalformedURLException {
        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName;
        }
        return new URL(JAR_URL_PROTOCOL, "", toURL(localResource).toExternalForm()
                + "!" + resourceName);
    }
    
    /**
     * Converts a remote URL and resource name to a the local prefetched URL.
     *
     * @param remoteUrlBase
     *            The remote base JAR url, like jar:file:/some/folder/important.jar!/
     * @param name
     *            The resource name.
     * @return The local JAR URL, for example,
     *         jar:file:/tmp/hudson-remoting1234/important.jar!/images/cat.png.
     * @throws IOException if any
     */
    private URL getLocalPrefetchedUrl(URL remoteUrlBase, String name) throws IOException {
        File localJarFile = prefetchedJars.get(prefetchedJarKey(remoteUrlBase));

        if (!localJarFile.exists()) {
            // It was deleted from underneath. Note that the unit test currently
            // does not test for this case. Due to Sun bug 4950148, you cannot
            // simulate the JAR being deleted since the URLClassLoader instance
            // keeps the file open. For now, this code was manually tested by
            // forcing the if statement to be executed in the debugger.
            // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4950148
            prefetchedJars.remove(prefetchedJarKey(remoteUrlBase));
            prefetchJar(remoteUrlBase);
            localJarFile = prefetchedJars.get(prefetchedJarKey(remoteUrlBase));
        }
        // Should we check if null and re-prefetch?
        return toJarURL(localJarFile, name);
    }

    /**
     * Converts a URL into a key that is used to store a map of remote URLs to their local file
     * equivalents.
     *
     * @param url The file URL to a JAR, for example: file:/some/folder/important.jar.
     * @return The key as a String.
     */
    private String prefetchedJarKey(URL url) {
        return Which.decode(url.getPath());
    }
    
    /**
     * Converts a URL into a key that is used to store a map of remote resource URLs to their local
     * file equivalents.
     *
     * @param url The file URL to a resource in a JAR, for example:
     * file:/some/folder/important.jar?/images/logo.gif
     * @return The key as a String.
     */
    private String resourcesMapKey(URL url) {
        return Which.decode(url.getFile());
    }

}
