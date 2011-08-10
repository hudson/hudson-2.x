/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi
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

import hudson.remoting.ChannelRunner.InProcess;
import hudson.remoting.RemoteClassLoader.ClassLoaderProxy;
import hudson.remoting.RemoteClassLoader.IClassLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * Tests the RemoteResourceManager class.
 */
public class RemoteResourceManagerTest extends TestCase {

    /**
     * The prefix of the test resources created. The resources will be named
     * resource1.txt..resourcen.txt
     */
    private static final String RESOURCE_NAME_PREFIX = "resource";

    /**
     * The URL resources that are created for the tests. It includes some JARs and unpackaged
     * resources.
     */
    private static final URL[] URLs = RemoteResourceTestUtilities.createResources();

    /** A temp folder where the JARs above are stored. */
    private static final String TEMP_FILE_NAME = "RemoteResourceManager Test";

    /** A class loader user for loading in the JARs that are created above. */
    private ClassLoader urlClassLoader;

    /** The channel used in the RemoteResourceManager test instance. */
    private Channel channel;

    /** A helper instance that creates a channel used for the test. */
    private InProcess inProcessChannelRunner;

    /** The test instance. */
    private RemoteResourceManager remoteResourceManager;

    @Override
    public void setUp() throws Exception {
        inProcessChannelRunner = new InProcess();
        channel = inProcessChannelRunner.start();
        urlClassLoader = new URLClassLoader(URLs);
    }

    @Override
    public void tearDown() throws Exception {
        inProcessChannelRunner.stop(channel);
    }

    /**
     * Tests that JARs can be prefetched and that JARs already prefetched are not repeated.
     */
    public void testPrefetch() throws IOException {
        RemoteResourceManager mgr = createRemoteResourceManager();
        URL jar = RemoteResourceTestUtilities.findJarToPrefetch(urlClassLoader);

        assertTrue(mgr.prefetchJar(jar));

        // A second prefetch on the same JAR, should return false
        assertFalse(mgr.prefetchJar(jar));

        // Convert any escaped spaces into actual spaces and it should still
        // see it as prefetched.
        jar = new URL(jar.getProtocol(), jar.getHost(), jar.getFile().replaceAll(
                "%20", " "));
        assertFalse(mgr.prefetchJar(jar));

        // Convert any spaces into escaped spaces and it should still see it as
        // prefetched.
        jar = new URL(jar.getProtocol(), jar.getHost(), jar.getFile().replaceAll(" ",
                "%20"));
        assertFalse(mgr.prefetchJar(jar));

    }

    /** Tests that a resource not in JARs can be found. */
    public void testUnpackagedResourceNoPrefetch() throws IOException {
        findUnpackagedResource();
    }

    /** Tests that resources not in JARs can be found. */
    public void testUnpackagedResourcesNoPrefetch() throws IOException {
        Set<String> resources = findUnpackagedResources();
        for (String urlspec : resources) {
            RemoteResourceTestUtilities.verifyIsLocalResource(new URL(urlspec));
        }
    }

    /** Tests that a resource not in JARs can be found when JARs have been prefetched. */
    public void testUnpackagedResourceWithPrefetch() throws IOException {
        findResourceWithPrefetch();
        findUnpackagedResource();
    }

    /** Tests that resources not in JARs can be found when JARs have been prefetched. */
    public void testUnpackagedResourcesWithPrefetch() throws IOException {
        findResourcesWithPrefetch();
        Set<String> resources = findUnpackagedResources();
        assertEquals(3, resources.size());

        boolean foundPrefetchedResource = false;
        boolean[] foundLocalResource = new boolean[] {false, false};
        int foundLocalResourceIndex = 0;

        for (String urlspec : resources) {
            if (urlspec.indexOf(".jar") == -1) {
                foundLocalResource[foundLocalResourceIndex] = true;
                foundLocalResourceIndex++;
                RemoteResourceTestUtilities.verifyIsLocalResource(new URL(urlspec));
            } else {
                foundPrefetchedResource = true;
                RemoteResourceTestUtilities.verifyIsFromPrefetchedJar(Which.jarFile(new URL(urlspec)));
            }
        }

        assertTrue(foundPrefetchedResource);
        assertTrue(foundLocalResource[0]);
        assertTrue(foundLocalResource[1]);
    }

    /**
     * Tests that a resource can be found when no JARs are prefetched.
     */
    public void testFindResourceNoPrefetch() throws Exception {
        findResourceWithoutPrefetch();
    }

    /**
     * Tests that a resource is found when its JAR is previously prefetched.
     */
    public void testFindResourceWhenPrefetchedFirst() throws IOException {
        findResourceWithPrefetch();
    }

    /**
     * Verifies that a resource is first found as a locally stored individual resource. Later, its
     * JAR is prefetched, so the test verifies that subsequent lookups are found within the
     * prefetched JAR.
     */
    public void testFindResourceWhenPrefetchedLater() throws IOException {
        findResourceWithoutPrefetch();
        findResourceWithPrefetch();
    }

    /**
     * Tests that resources can be found when no JARs are prefetched.
     */
    public void testFindResourcesWithoutPrefetch() throws IOException {
        findResourcesWithoutPrefetch();
    }

    /**
     * Tests that resources are found when some of the JARs are previously prefetched.
     */
    public void testFindResourcesWhenPrefetchedFirst() throws IOException {
        findResourcesWithPrefetch();
    }

    /**
     * Verifies that resources are first found as a locally stored individual resources. Later, some
     * of the JARs are prefetched, so the test verifies that subsequent lookups are found within the
     * prefetched JAR where applicable.
     */
    public void testFindResourcesWhenPrefetchedLater() throws IOException {
        findResourcesWithoutPrefetch();
        findResourcesWithPrefetch();
    }

    /**
     * Tests the retrieve of multiple resources from the same JAR. Verifies that the expected
     * content is found.
     */
    public void testMultipleResourcesInSameJAR() throws IOException {
        RemoteResourceManager mgr = createRemoteResourceManager();
        Vector<URL> resource0Resources = RemoteResourceTestUtilities.enumerationToVector(mgr.findResources(RESOURCE_NAME_PREFIX
                + "0.txt"));
        Vector<URL> resource1Resources = RemoteResourceTestUtilities.enumerationToVector(mgr.findResources(RESOURCE_NAME_PREFIX
                + "1.txt"));

        assertEquals(2, resource0Resources.size());
        assertEquals(3, resource1Resources.size());

        // All of the content should be unique
        Set<String> content = new HashSet<String>();
        content.add(new String(RemoteResourceTestUtilities.readFully(resource0Resources.get(0).openStream())));
        content.add(new String(RemoteResourceTestUtilities.readFully(resource0Resources.get(1).openStream())));
        content.add(new String(RemoteResourceTestUtilities.readFully(resource1Resources.get(0).openStream())));
        content.add(new String(RemoteResourceTestUtilities.readFully(resource1Resources.get(1).openStream())));
        content.add(new String(RemoteResourceTestUtilities.readFully(resource1Resources.get(2).openStream())));
        // If any of the content is unique, then the set will be smaller than five.
        assertEquals(5, content.size());

        assertTrue(content.contains("foo"));
        assertTrue(content.contains("bar"));
        assertTrue(content.contains("baz"));
        assertTrue(content.contains("hello"));
        assertTrue(content.contains("unpackaged resource content resource1"));
    }

    /**
     * Helper method that verifies that a resource can be found when no JARs are prefetched.
     *
     * @return The URL to the resource that was found.
     */
    private URL findResourceWithoutPrefetch() throws IOException {
        String resourceName = "/META-INF/MANIFEST.MF";

        RemoteResourceManager mgr = createRemoteResourceManager();
        URL resource = mgr.findResource(resourceName);
        Manifest actualManifest = new Manifest(resource.openStream());
        assertEquals(RemoteResourceTestUtilities.createManifestFile(), actualManifest);
        RemoteResourceTestUtilities.verifyIsLocalResource(resource);

        // Verify that it is found in the same local spot when retrieved again.
        URL secondLookup = mgr.findResource(resourceName);
        assertEquals(resource.toExternalForm(), secondLookup.toExternalForm());
        actualManifest = new Manifest(resource.openStream());
        assertEquals(RemoteResourceTestUtilities.createManifestFile(), actualManifest);

        // Verify that if deleted from underneath, it is recreated.
        File resourceFile = new File(resource.getFile());
        resourceFile.delete();
        URL thirdLookup = mgr.findResource(resourceName);
        assertNotSame(resource.toExternalForm(), thirdLookup.toExternalForm());
        actualManifest = new Manifest(thirdLookup.openStream());
        assertEquals(RemoteResourceTestUtilities.createManifestFile(), actualManifest);

        return thirdLookup;
    }

    /**
     * Helper method that verifies that a resource is found when its JAR is previously prefetched.
     *
     * @return The URL to the resource that was found.
     */
    private URL findResourceWithPrefetch() throws MalformedURLException, IOException {
        RemoteResourceManager mgr = createRemoteResourceManager();
        String resourceName = "/META-INF/MANIFEST.MF";

        URL expectedResource = urlClassLoader.getResource(resourceName);

        URL jar = RemoteResourceTestUtilities.findJarToPrefetch(urlClassLoader);
        assertTrue(mgr.prefetchJar(jar));

        URL actualResource = mgr.findResource(resourceName);

        File remotePath = Which.jarFile(expectedResource);
        File localPath = Which.jarFile(actualResource);

        // Verify that the resources came from different URLs
        assertNotSame(remotePath.getParent(), localPath.getParent());
        RemoteResourceTestUtilities.verifyIsFromPrefetchedJar(localPath);

        byte[] expectedImage = RemoteResourceTestUtilities.readFully(expectedResource.openStream());
        byte[] actualImage = RemoteResourceTestUtilities.readFully(actualResource.openStream());
        assertTrue(Arrays.equals(expectedImage, actualImage));

        return actualResource;
    }

    /** Finds an unpackaged resource (i.e. resources not in JARs). */
    public void findUnpackagedResource() throws IOException {
        String resource = RESOURCE_NAME_PREFIX + "3.txt";
        URL expectedResource = urlClassLoader.getResource(resource);
        RemoteResourceManager mgr = createRemoteResourceManager();
        URL actualResource = mgr.findResource(resource);
        assertNotNull(actualResource);
        byte[] expectedImage = RemoteResourceTestUtilities.readFully(expectedResource.openStream());
        byte[] actualImage = RemoteResourceTestUtilities.readFully(actualResource.openStream());
        assertTrue(Arrays.equals(expectedImage, actualImage));

        // Verify that it is in the same spot upon subsequent lookups.
        assertEquals(actualResource.toExternalForm(), mgr.findResource(resource).toExternalForm());
    }

    /** Finds unpackaged resources (i.e. resources not in JARs). */
    public Set<String> findUnpackagedResources() throws IOException {
        String resource = RESOURCE_NAME_PREFIX + "1.txt";
        Enumeration<URL> expectedResources = urlClassLoader.getResources(resource);
        Set<String> expectedResourcesContent = new HashSet<String>();

        // Get the content of the expected resources.
        while (expectedResources.hasMoreElements()) {
            URL curr = expectedResources.nextElement();
            String content = new String(RemoteResourceTestUtilities.readFully(curr.openStream()));
            expectedResourcesContent.add(content);
        }

        assertEquals(3, expectedResourcesContent.size());

        RemoteResourceManager mgr = createRemoteResourceManager();
        Enumeration<URL> actualResources = mgr.findResources(resource);
        Set<String> firstLookupSorted = new HashSet<String>();

        while (actualResources.hasMoreElements()) {
            URL curr = actualResources.nextElement();
            String content = new String(RemoteResourceTestUtilities.readFully(curr.openStream()));
            assertTrue(expectedResourcesContent.contains(content));
            firstLookupSorted.add(curr.toExternalForm());
        }

        assertFalse(actualResources.hasMoreElements());

        actualResources = mgr.findResources(resource);
        Set<String> secondLookupSorted = new HashSet<String>();
        while (actualResources.hasMoreElements()) {
            URL curr = actualResources.nextElement();
            secondLookupSorted.add(curr.toExternalForm());
        }

        return firstLookupSorted;
    }

    /**
     * Helper method that verifies that resources are be found when no JARs are prefetched.
     *
     * @return A Vector of URLs to the resources that were found.
     */
    private Vector<URL> findResourcesWithoutPrefetch() throws IOException, MalformedURLException {
        String resourceName = RESOURCE_NAME_PREFIX + "1.txt";

        RemoteResourceManager mgr = createRemoteResourceManager();
        Vector<URL> resources = RemoteResourceTestUtilities.enumerationToVector(mgr.findResources(resourceName));
        RemoteResourceTestUtilities.verifyIsLocalResource(resources.get(0));
        RemoteResourceTestUtilities.verifyIsLocalResource(resources.get(1));

        RemoteResourceTestUtilities.verifyResourcesEqual(resources);

        Vector<URL> secondLookup = RemoteResourceTestUtilities.enumerationToVector(mgr.findResources(resourceName));
        assertEquals(resources.get(0), secondLookup.get(0));
        assertEquals(resources.get(1), secondLookup.get(1));

        RemoteResourceTestUtilities.verifyResourcesEqual(secondLookup);

        // Verify that if deleted from underneath, it is recreated.
        File resourceFile = new File(resources.get(0).getFile());
        resourceFile.delete();
        resourceFile = new File(resources.get(1).getFile());
        resourceFile.delete();

        Vector<URL> thirdLookup = RemoteResourceTestUtilities.enumerationToVector(mgr.findResources(resourceName));
        RemoteResourceTestUtilities.verifyResourcesEqual(thirdLookup);

        // Since the files had to be recreated, the underlying URLs should be different.
        assertNotSame(resources.get(0).toExternalForm(), secondLookup.get(0).toExternalForm());
        assertNotSame(resources.get(1).toExternalForm(), secondLookup.get(1).toExternalForm());

        return resources;
    }

    /**
     * Helper method that verifies that resources are found when some of the applicable JAR are
     * previously prefetched.
     *
     * @return A Vector of URLs to the resources that were found.
     */
    private Vector<URL> findResourcesWithPrefetch() throws IOException {
        RemoteResourceManager mgr = createRemoteResourceManager();
        URL jar = RemoteResourceTestUtilities.findJarToPrefetch(urlClassLoader);

        assertTrue(mgr.prefetchJar(jar));

        String resourceName = RESOURCE_NAME_PREFIX + "1.txt";
        Vector<URL> resources = RemoteResourceTestUtilities.enumerationToVector(mgr.findResources(resourceName));

        File prefetchedJar = Which.jarFile(resources.get(0));
        RemoteResourceTestUtilities.verifyIsFromPrefetchedJar(prefetchedJar);
        RemoteResourceTestUtilities.verifyIsLocalResource(resources.get(1));
        RemoteResourceTestUtilities.verifyResourcesEqual(resources);

        return resources;
    }

    /**
     * Create the RemoteResourceManager test instance.
     *
     * @return The RemoteResourceManager instance.
     */
    private RemoteResourceManager createRemoteResourceManager() {
        if (remoteResourceManager == null) {
            ClassLoaderProxy classLoaderProxy =
                new ClassLoaderProxy(new URLClassLoader(URLs), channel);

            RemoteInvocationHandler invoker = new RemoteInvocationHandler(
                    classLoaderProxy);
            IClassLoader proxy = (IClassLoader) Proxy.newProxyInstance(IClassLoader.class
                    .getClassLoader(), new Class[] { IClassLoader.class }, invoker);

            RemoteClassLoader remoteClassLoader = (RemoteClassLoader) RemoteClassLoader.create(
                    new URLClassLoader(new URL[0]), proxy);
            remoteResourceManager = new RemoteResourceManager(remoteClassLoader, proxy, channel);
        }

        return remoteResourceManager;
    }

    /**
     * A remote invocation handler for the proxy class loader. This handler just invokes the
     * supplied method using the supplied arguments.
     */
    private static class RemoteInvocationHandler implements InvocationHandler {

        private final ClassLoaderProxy cl;

        public RemoteInvocationHandler(ClassLoaderProxy cl) {
            this.cl = cl;
        }

        public Object invoke(Object object, Method method, Object[] args) throws Throwable {
            return method.invoke(cl, args);
        }
    }
}
