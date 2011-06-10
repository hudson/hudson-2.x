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
