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
 * Utility supplying different ways to format a version number.
 *  
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
class VersionFormatter
{
    /**
     * @return a formatted version number containing the raw information
     */
    public static String asRawVersion( String version, String timestamp, String sequence )
    {
        StringBuilder buff = new StringBuilder();

        if ( version == null )
        {
            version = VersionSupport.UNKNOWN;
        }
        buff.append( version );

        if ( timestamp != null )
        {
            buff.append( "," ).append( timestamp );
        }

        if ( sequence != null )
        {
            buff.append( "#" ).append( sequence );
        }

        return buff.toString();
    }

    /**
     * Generates an appropriate format based on the project being a release or development version. Example:
     * 0.2.1,201009291818#1
     * 
     * @return a formatted version number in the simplest and most significant form
     */
    public static String asCanonicalVersion( String version, String timestamp, String sequence )
    {
        String derivedVersion = version;
        if ( derivedVersion == null )
        {
            derivedVersion = VersionSupport.UNKNOWN;
        }

        String snapshot = "-SNAPSHOT";
        if ( derivedVersion.contains( snapshot ) )
        {
            derivedVersion = asRawVersion( version.replace( snapshot, "" ), timestamp, sequence );
        }

        return derivedVersion;
    }
}
