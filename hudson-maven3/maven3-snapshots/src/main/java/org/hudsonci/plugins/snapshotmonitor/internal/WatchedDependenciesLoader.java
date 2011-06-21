/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.plugins.snapshotmonitor.internal;

import org.hudsonci.utils.io.Closer;
import org.hudsonci.utils.marshal.Marshaller;
import org.hudsonci.utils.marshal.XStreamMarshaller;
import com.thoughtworks.xstream.XStream;
import hudson.model.Job;

import org.hudsonci.plugins.snapshotmonitor.model.WatchedDependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loads (and stores) project watched dependencies details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class WatchedDependenciesLoader
{
    private static final Logger log = LoggerFactory.getLogger(WatchedDependenciesLoader.class);

    public static final String FILE_NAME = "watched-dependencies.xml";

    private final Marshaller marshaller;

    public WatchedDependenciesLoader() {
        XStream xs = new XStream();
        xs.setClassLoader(getClass().getClassLoader());
        xs.processAnnotations(WatchedDependencies.class);
        xs.addDefaultImplementation(HashSet.class,Collection.class);
        marshaller = new XStreamMarshaller(xs);
    }

    private File getFile(final Job job) {
        return new File(job.getRootDir(), FILE_NAME);
    }

    public WatchedDependencies load(final Job job) throws IOException {
        checkNotNull(job);

        File file = getFile(job);
        if (file.exists()) {
            log.debug("Loading from file: {}", file);
            Reader reader = new BufferedReader(new FileReader(file));
            try {
                return (WatchedDependencies) marshaller.unmarshal(reader);
            }
            finally {
                Closer.close(reader);
            }
        }

        return new WatchedDependencies();
    }

    public void store(final Job job, final WatchedDependencies dependencies) throws IOException {
        checkNotNull(job);
        checkNotNull(dependencies);

        File file = getFile(job);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("Failed to create directory structure: {}", dir);
            }
        }

        log.debug("Storing to file: {}", file);
        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            marshaller.marshal(dependencies, writer);
        }
        finally {
            Closer.close(writer);
        }
    }
}
