/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import hudson.util.ClasspathBuilder;
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
            args.add("-Dmatrix.eventspy=whatever");
            assertFalse(builder.detectBannedProperties(args));
        }
    }
}
