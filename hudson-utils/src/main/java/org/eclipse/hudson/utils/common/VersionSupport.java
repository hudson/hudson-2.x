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
