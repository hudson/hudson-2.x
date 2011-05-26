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

package org.hudsonci.utils.tasks;

import hudson.model.Hudson;
import hudson.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores a {@link UUID} for each job. UUID is stored as <tt>uuid</tt> file
 * under the projects root directory. Value is cached once determined.
 *
 * JobProperty mechanism is faulty at best, can not use it to store these
 * values. Project renames, where applicable, should also include
 * moving/renaming of the UUID file.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class JobUuid
{
    private static final Logger log = LoggerFactory.getLogger(JobUuid.class);

    private static final Map<Job<?, ?>, UUID> cache = new WeakHashMap<Job<?, ?>, UUID>();

    /**
     * Get the UUID for the given Job. If the UUID is missing a new UUID is
     * created/persisted for the Job.
     */
    public static UUID get(final Job<?, ?> job) {
        checkNotNull(job);

        UUID id;

        synchronized (cache) {
            id = cache.get(job);
            if (id == null) {
                File file = new File(job.getRootDir(), "uuid");
                if (file.exists()) {
                    log.debug("Loading UUID from file: {}", file);
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        try {
                            String line = reader.readLine();
                            id = UUID.fromString(line);
                        } finally {
                            reader.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    log.debug("Missing UUID for job: {}; writing new UUID to file: {}", job, file);
                    // In some cases, such as for copied jobs, the file path may not exist yet. 
                    file.getParentFile().mkdirs();

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        try {
                            id = UUID.randomUUID();
                            writer.write(id.toString());
                            writer.flush();
                        } finally {
                            writer.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                cache.put(job, id);
            }
        }

        return id;
    }

    /**
     * Find the Job with the given UUID, if it exists.
     */
    public static Job<?, ?> find(final UUID uuid) {
        checkNotNull(uuid);

        synchronized (cache) {
            for (Job job : Hudson.getInstance().getAllItems(Job.class)) {
                UUID id = get(job);
                if (id != null && uuid.equals(id)) {
                    return job;
                }
            }
        }

        return null;
    }

    public static Job<?, ?> find(final String uuid) {
        return find(UUID.fromString(uuid));
    }

    // FIXME: May actually want to use non-url-safe tokens '{' and '}' (or something) here to avoid user creating project with name that matches format.

    public static final String ENCODED_PREFIX = "UU-";

    public static final String ENCODED_SUFFIX = "-ID";

    /**
     * Encodes the given UUID as <tt>UU-<em>uuid-value</em>-ID</em>.
     */
    public static String encode(final UUID uuid) {
        checkNotNull(uuid);
        return String.format("%s%s%s", ENCODED_PREFIX, uuid, ENCODED_SUFFIX);
    }

    /**
     * @see #encode(UUID)
     */
    public static UUID decode(final String spec) {
        checkNotNull(spec);

        if (spec.startsWith(ENCODED_PREFIX) && spec.endsWith(ENCODED_SUFFIX)) {
            String tmp = spec.substring(ENCODED_PREFIX.length(), spec.length() - ENCODED_SUFFIX.length());
            return UUID.fromString(tmp);
        }

        throw new IllegalArgumentException("Non-encoded UUID string: " + spec);
    }
}
