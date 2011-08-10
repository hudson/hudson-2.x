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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

public class RemoteClassLoaderTest extends RmiTestBase {

    /**
     * The prefix of the test resources created. The resources will be named
     * resource1.txt..resourcen.txt
     */
    private static final String RESOURCE_NAME_PREFIX = RemoteResourceTestUtilities.RESOURCE_NAME_PREFIX;

    /**
     * The URL resources that are created for the tests. It includes some JARs and unpackaged
     * resources.
     */
    private static final URL[] URLs = RemoteResourceTestUtilities.createResources();

    /** A temp folder where the JARs above are stored. */
    private static final String TEMP_FILE_NAME = RemoteResourceTestUtilities.TEMP_FILE_NAME;

    /** A class loader user for loading in the JARs that are created above. */
    private ClassLoader urlClassLoader;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        urlClassLoader = new URLClassLoader(URLs);
    }

    public void testFindResourcesWithPrefetch() throws IOException, InterruptedException {
        URL jar = RemoteResourceTestUtilities.findJarToPrefetch(urlClassLoader);

        URL[] jars = new URL[] { jar };

        PreloadJarTask preloadJarTask = new PreloadJarTask(jars, urlClassLoader);
        channel.call(preloadJarTask);

        DummyTask task = new DummyTask(urlClassLoader);
        channel.call(task);
    }

    private static class DummyTask implements DelegatingCallable<Boolean, IOException> {

        private static final long serialVersionUID = -814903050393989045L;

        private transient ClassLoader target;

        public DummyTask(ClassLoader target) {
            this.target = target;
        }

        public ClassLoader getClassLoader() {
            return target;
        }

        public Boolean call() throws IOException {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (!(cl instanceof RemoteClassLoader)) {
                return false;
            }

            RemoteClassLoader rcl = (RemoteClassLoader) cl;
            String resourceName = RESOURCE_NAME_PREFIX + "1.txt";
            Vector<URL> resources = RemoteResourceTestUtilities.enumerationToVector(rcl.getResources(resourceName));

            assertEquals(3, resources.size());

            // One of the resources should be from a prefetched JAR; the others should be locally
            // cached copies.
            int prefetchedCount = 0;
            for (int i = 0; i < resources.size(); i++) {
                String urlspec = resources.get(i).toExternalForm();
                if (urlspec.indexOf(".jar") != -1) {
                    prefetchedCount++;
                }
            }

            assertEquals("Expect one URL to be from a prefetched JAR.", 1, prefetchedCount);

            RemoteResourceTestUtilities.verifyResourcesEqual(resources);

            return true;
        }
    }
}
