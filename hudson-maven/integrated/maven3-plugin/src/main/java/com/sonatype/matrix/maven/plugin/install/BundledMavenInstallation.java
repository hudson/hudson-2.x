/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.install;

import hudson.FilePath;

import java.io.IOException;

/**
 * Bundled {@link MavenInstallation}, for use as default and fall-back.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class BundledMavenInstallation
    extends MavenInstallation
{
    public static final String NAME = "(Bundled)";

    public BundledMavenInstallation() throws IOException, InterruptedException {
        super(NAME, getLocation().getRemote(), null);
    }

    public static FilePath getLocation() throws IOException, InterruptedException {
        return SlaveBundleInstaller.getInstallRoot().child("bundled-maven");
    }
}