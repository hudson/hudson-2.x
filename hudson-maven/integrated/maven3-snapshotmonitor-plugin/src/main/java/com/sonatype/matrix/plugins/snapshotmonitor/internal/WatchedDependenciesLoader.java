/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor.internal;

import com.sonatype.matrix.common.io.Closer;
import com.sonatype.matrix.common.marshal.Marshaller;
import com.sonatype.matrix.common.marshal.XStreamMarshaller;
import com.sonatype.matrix.plugins.snapshotmonitor.model.WatchedDependencies;
import com.thoughtworks.xstream.XStream;
import hudson.model.Job;
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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loads (and stores) project watched dependencies details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Singleton
public class WatchedDependenciesLoader
{
    private static final Logger log = LoggerFactory.getLogger(WatchedDependenciesLoader.class);

    public static final String FILE_NAME = "watched-dependencies.xml";

    private final Marshaller marshaller;

    public WatchedDependenciesLoader() {
        XStream xs = new XStream();
        xs.processAnnotations(WatchedDependencies.class);
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