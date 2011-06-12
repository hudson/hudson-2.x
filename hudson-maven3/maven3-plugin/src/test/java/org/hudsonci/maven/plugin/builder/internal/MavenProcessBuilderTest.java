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

import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import hudson.util.ClasspathBuilder;

import org.hudsonci.maven.plugin.builder.internal.MavenProcessBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link MavenProcessBuilder}.
 */
public class MavenProcessBuilderTest
{
    private MavenProcessBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new MavenProcessBuilder();
    }

    @Test
    public void testFullyConfigured() throws Exception {
        builder.withWindows(false);

        BuildConfigurationDTO config = new BuildConfigurationDTO();
        builder.withConfiguration(config);

        FilePath mavenHome = new FilePath(new File("maven-home"));
        builder.withMavenHome(mavenHome);
        builder.withMavenExecutable(mavenHome.child("bin/mvn"));

        ClasspathBuilder cp = new ClasspathBuilder();
        cp.add("foo").add("bar");
        builder.withExtClasspath(cp);

        Map<String,String> vars = new HashMap<String,String>();
        builder.withBuildVariables(vars);

        EnvVars env = new EnvVars();
        builder.withEnv(env);

        FilePath workingDir = new FilePath(new File("working-dir"));
        builder.withWorkingDirectory(workingDir);

        FilePath repoDir = new FilePath(new File("repo-dir"));
        builder.withRepository(repoDir);

        OutputStream output = new ByteArrayOutputStream();
        builder.withStandardOutput(output);

        int port = 666;
        builder.withPort(port);

        System.out.println("ARGS: " + builder.buildArguments());
        System.out.println("OPTS: " + builder.buildOpts());
        System.out.println("ENV: " + builder.buildEnv());
    }

    @Test
    public void testDetectBannedOptions() {
        for (String arg : MavenProcessBuilder.BANNED_OPTIONS) {
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(arg);
            assertTrue(builder.detectBannedOptions(args));
        }

        // Happy not on the banned list
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add("-V");
        assertFalse(builder.detectBannedOptions(args));
    }

    @Test
    public void testDetectBannedProperties() {
        for (String arg : MavenProcessBuilder.BANNED_PROPERTIES) {
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(arg);
            assertTrue(builder.detectBannedProperties(args));
        }

        // Happy not on the banned list
        {
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add("-Dfoo=bar");
            assertFalse(builder.detectBannedProperties(args));
        }

        // This is valid, though pointless
        {
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add("-Dhudson.eventspy=whatever");
            assertFalse(builder.detectBannedProperties(args));
        }
    }
}
