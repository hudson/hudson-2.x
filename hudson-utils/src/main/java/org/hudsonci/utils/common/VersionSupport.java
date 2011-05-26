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

/**
 * Support for exposing version details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class VersionSupport
{
    public static final String RESOURCE_NAME = "version.properties";

    public static final String UNKNOWN = "unknown";

    public static final String VERSION = "version";

    public static final String TIMESTAMP = "timestamp";

    public static final String SEQUENCE = "sequence";

    private final PropertiesLoader props;

    protected VersionSupport() {
        this.props = new PropertiesLoader(this, RESOURCE_NAME).load();
    }

    public String getVersion() {
        return props.getValue(VERSION);
    }

    public String getTimestamp() {
        return props.getValue(TIMESTAMP);
    }

    public String getSequence() {
        return props.getValue(SEQUENCE);
    }

    /**
     * @return a formatted version number in the simplest and most significant form
     */
    public String getCanonicalVersion()
    {
        // FIXME cache the formatted version since users may be accessing it frequently?
        // e.g. on every page of the web ui.
        return VersionFormatter.asCanonicalVersion( getVersion(), getTimestamp(), getSequence() );
    }

    /**
     * @return a formatted version number containing the raw information
     */
    public String getRawVersion()
    {
        return VersionFormatter.asRawVersion( getVersion(), getTimestamp(), getSequence() );
    }

    @Override
    public String toString() {
        return getCanonicalVersion();
    }
}
