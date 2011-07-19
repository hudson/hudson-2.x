/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.plugins.snapshotmonitor.internal;

import hudson.util.IOException2;

import org.eclipse.hudson.utils.io.Closer;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.eclipse.hudson.plugins.snapshotmonitor.MetadataChecker;
import org.eclipse.hudson.plugins.snapshotmonitor.model.WatchedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Checks {@link Metadata} instances for the last updated time.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class SimpleMetadataChecker
    implements MetadataChecker
{
    private static final Logger log = LoggerFactory.getLogger(SimpleMetadataChecker.class);

    /** The URL of the Maven repository. */
    private final URL baseUrl;

    /** Http basic auth value; if user/pass configured. */
    private final String auth;

    public SimpleMetadataChecker(String baseUrl, final String user, final String password) {
        assert baseUrl != null;

        // Append / to URL if it needs one, else URL(URL,String) won't behave as we want
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        try {
            this.baseUrl = new URL(baseUrl);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // If we need auth, then setup the Authorization header value
        if (user != null && password != null) {
            this.auth = "Basic " + new Base64Encoder().encode((user + ":" + password).getBytes());
        }
        else {
            this.auth = null;
        }
    }

    private Metadata load(final String path) throws IOException {
        assert path != null;

        URL url = new URL(baseUrl, path);
        log.debug("Checking: {} ({})", url, path);

        URLConnection c = url.openConnection();
        if (auth != null) {
            c.setRequestProperty("Authorization", auth);
        }

        MetadataXpp3Reader reader = new MetadataXpp3Reader();
        BufferedReader input = new BufferedReader(new InputStreamReader(c.getInputStream()));
        Metadata data;
        try {
            data = reader.read(input, false);
        }
        catch (Exception e) {
            throw new IOException2("Failed to parse: " + path, e);
        }
        finally {
            Closer.close(input);

            try {
                if (c instanceof HttpURLConnection) {
                    HttpURLConnection.class.cast(c).disconnect();
                }
            }
            catch (Exception ignore) {
                // ignore
            }
        }

        return data;
    }

    public String getPath(final MavenCoordinatesDTO artifact) {
        assert artifact != null;
        return String.format("%s/%s/%s/maven-metadata.xml", artifact.getGroupId().replace('.', '/'), artifact.getArtifactId(), artifact.getVersion());
    }

    private Metadata load(final WatchedDependency dependency) throws IOException {
        assert dependency != null;
        return load(getPath(dependency));
    }

    /**
     * Returns the last-updated date for the given dependency, or 0 if unavailable.
     */
    public long check(final WatchedDependency dependency) throws IOException {
        Metadata md = load(dependency);
        if (md.getVersioning() == null) {
            return 0;
        }
        String tmp = md.getVersioning().getLastUpdated();
        log.trace("Last-updated: {}", tmp);
        if (tmp == null) {
            return 0;
        }
        return Long.parseLong(tmp);
    }
}
