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

package org.eclipse.hudson.utils.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for loading resource-based properties file.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PropertiesLoader
{
    private final Properties props = new Properties();

    private final Class owner;

    private final String resourceName;

    public PropertiesLoader(final Class owner, final String resourceName) {
        this.owner = checkNotNull(owner);
        this.resourceName = checkNotNull(resourceName);
    }

    public PropertiesLoader(final Object owner, final String resourceName) {
        this(owner.getClass(), resourceName);
    }

    public Properties getProperties() {
        return props;
    }

    public String getResourceName() {
        return resourceName;
    }

    public PropertiesLoader load() {
        try {
            InputStream input = getResource().openStream();
            try {
                props.load(input);
            }
            finally {
                input.close();
            }
        }
        catch (IOException e) {
            throw new Error("Failed to load properties", e);
        }
        return this;
    }

    public URL getResource() {
        String name = getResourceName();
        URL url = owner.getResource(name);
        if (url == null) {
            throw new Error("Unable to load resource: " + name);
        }
        return url;
    }

    public String getValue(final String name, final String defaultValue) {
        String value = getProperties().getProperty(name);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String getValue(final String name) {
        return getValue(name, null);
    }

    @Override
    public String toString() {
        return props.toString();
    }
}
