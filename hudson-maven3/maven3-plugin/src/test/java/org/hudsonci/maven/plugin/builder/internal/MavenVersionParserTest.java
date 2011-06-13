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

package org.hudsonci.maven.plugin.builder.internal;

import org.hudsonci.maven.plugin.builder.internal.MavenVersionParser;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MavenVersionParser}.
 */
public class MavenVersionParserTest
{
    private MavenVersionParser parser;

    @Before
    public void setUp() throws Exception {
        this.parser = new MavenVersionParser();
    }

    private void assertParsedMavenVersion(final String expect, final String input) throws IOException {
        String found = parser.parse(input);
        System.out.println(found);
        assertEquals(expect, found);
    }

    @Test
    public void testDetectVersion() throws Exception {
        assertParsedMavenVersion(
            "3.0.2",
            "Apache Maven 3.0.2 (r1056850; 2011-01-08 16:58:10-0800)\n" +
                "Java version: 1.6.0_22, vendor: Apple Inc.\n" +
                "Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home\n" +
                "Default locale: en_US, platform encoding: MacRoman\n" +
                "OS name: \"mac os x\", version: \"10.6.6\", arch: \"x86_64\", family: \"mac\"\n"
        );
    }

    @Test
    public void testDetectVersionWithWarning() throws Exception {
        assertParsedMavenVersion(
            "3.0.2",
            "Warning: JAVA_HOME environment variable is not set.\n" +
                "Apache Maven 3.0.2 (r1056850; 2011-01-08 16:58:10-0800)\n" +
                "Java version: 1.6.0_22, vendor: Apple Inc.\n" +
                "Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home\n" +
                "Default locale: en_US, platform encoding: MacRoman\n" +
                "OS name: \"mac os x\", version: \"10.6.6\", arch: \"x86_64\", family: \"mac\"\n"
        );
    }

    @Test
    public void testDetectVersionWithSnapshot() throws Exception {
        assertParsedMavenVersion(
            "3.0.3-SNAPSHOT",
            "Apache Maven 3.0.3-SNAPSHOT (r1071029; 2011-02-15 12:09:10-0800)\n" +
                "Maven home: /home/hudson/slave1/maven/slavebundle/bundled-maven\n" +
                "Java version: 1.6.0_20, vendor: Sun Microsystems Inc.\n" +
                "Java home: /usr/lib/jvm/java-6-sun-1.6.0.20/jre\n" +
                "Default locale: en_US, platform encoding: UTF-8\n" +
                "OS name: \"linux\", version: \"2.6.32-22-server\", arch: \"amd64\", family: \"unix\"\n"
        );
    }

    @Test
    public void testDetectVersionWithWarningAndSnapshot() throws Exception {
        assertParsedMavenVersion(
            "3.0.3-SNAPSHOT",
            "Warning: JAVA_HOME environment variable is not set.\n" +
                "Apache Maven 3.0.3-SNAPSHOT (r1071029; 2011-02-15 12:09:10-0800)\n" +
                "Maven home: /home/hudson/slave1/maven/slavebundle/bundled-maven\n" +
                "Java version: 1.6.0_20, vendor: Sun Microsystems Inc.\n" +
                "Java home: /usr/lib/jvm/java-6-sun-1.6.0.20/jre\n" +
                "Default locale: en_US, platform encoding: UTF-8\n" +
                "OS name: \"linux\", version: \"2.6.32-22-server\", arch: \"amd64\", family: \"unix\"\n"
        );
    }

    @Test
    public void testDetectMaven2_2_1() throws Exception {
        assertParsedMavenVersion(
            "2.2.1",
            "Apache Maven 2.2.1 (r801777; 2009-08-06 12:16:01-0700)\n"+
                "Java version: 1.6.0_22\n"+
                "Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home\n"+
                "Default locale: en_US, platform encoding: MacRoman\n"+
                "OS name: \"mac os x\" version: \"10.6.6\" arch: \"x86_64\" Family: \"mac\"\n"
        );
    }

    @Test
    public void testDetectMaven2_0_11() throws Exception {
        assertParsedMavenVersion(
            "2.0.11",
            "Apache Maven 2.0.11 (r909250; 2010-02-11 21:55:50-0800)\n"+
                "Java version: 1.6.0_22\n"+
                "Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home\n"+
                "Default locale: en_US, platform encoding: MacRoman\n"+
                "OS name: \"mac os x\" version: \"10.6.6\" arch: \"x86_64\" Family: \"mac\"\n"
        );
    }

    // TODO: Add a few tests using windows output
}
