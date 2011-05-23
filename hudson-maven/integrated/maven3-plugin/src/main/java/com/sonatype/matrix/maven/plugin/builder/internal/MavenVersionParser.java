/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Parse Maven version from <tt>mvn --version</tt> output.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class MavenVersionParser
{
    // FIXME: This expression is still not specific enough
    private static final Pattern MAVEN_VERSION = Pattern.compile("(?i).*Maven [^0-9]*([0-9]\\S*).*");

    public String parse(final BufferedReader reader) throws IOException {
        checkNotNull(reader);
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher m = MAVEN_VERSION.matcher(line);
            if (m.matches()) {
                // First match wins.
                return m.group(1);
            }
        }

        return null;
    }

    public String parse(final String text) throws IOException {
        checkNotNull(text);
        return parse(new BufferedReader(new StringReader(text)));
    }
}