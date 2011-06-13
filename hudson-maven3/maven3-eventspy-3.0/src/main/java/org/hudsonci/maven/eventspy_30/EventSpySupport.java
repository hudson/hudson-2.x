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

package org.hudsonci.maven.eventspy_30;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link org.apache.maven.eventspy.EventSpy} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class EventSpySupport
    extends AbstractEventSpy
{
    // This is really more of a things for *-eventspy-common, but due to the use of Maven-specific
    // ... interfaces its living here, may change if/when needed

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Context context;

    protected boolean isOpen() {
        return context != null;
    }

    protected void ensureOpened() {
        if (!isOpen()) {
            throw new IllegalStateException();
        }
    }

    protected Context getContext() {
        ensureOpened();
        return context;
    }

    protected DefaultPlexusContainer getContainer() {
        return (DefaultPlexusContainer) getContext().getData().get("plexus");
    }

    protected File getWorkingDirectory() {
        return new File((String)getContext().getData().get("workingDirectory"));
    }

    protected Properties getSystemProperties() {
        return (Properties)getContext().getData().get("systemProperties");
    }

    protected Properties getUserProperties() {
        return (Properties)getContext().getData().get("userProperties");
    }

    protected Properties getVersionProperties() {
        return (Properties)getContext().getData().get("versionProperties");
    }

    /**
     * Get a configuration property value.
     */
    protected String getProperty(final String name, final String defaultValue) {
        checkNotNull(name);

        // For now we only look at system properties for configuration
        String value = getSystemProperties().getProperty(name);
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Get a configuration property value.
     */
    protected String getProperty(final String name) {
        return getProperty(name, null);
    }

    @Override
    public void init(final Context context) throws Exception {
        if (isOpen()) {
            throw new IllegalStateException();
        }
        this.context = checkNotNull(context);
    }

    @Override
    public void close() throws Exception {
        this.context = null;
    }
}
