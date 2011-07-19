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

package org.eclipse.hudson.maven.plugin.install;

import hudson.FilePath;

import java.io.IOException;

/**
 * Bundled {@link MavenInstallation}, for use as default and fall-back.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
