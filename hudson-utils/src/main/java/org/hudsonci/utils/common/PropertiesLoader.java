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

package org.hudsonci.utils.common;

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
