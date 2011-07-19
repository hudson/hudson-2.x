/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.Extension;
import hudson.util.DescriptorList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @see MavenReporter
 */
public final class MavenReporters {
    /**
     * List of all installed {@link MavenReporter}s.
     *
     * @deprecated as of 1.286. Use {@code MavenReporterDescriptor#all()} for listing reporters, and
     * use {@link Extension} for automatic registration. 
     */
    public static final List<MavenReporterDescriptor> LIST = (List)new DescriptorList<MavenReporter>(MavenReporter.class);

    /**
     * Gets the subset of {@link #LIST} that has configuration screen.
     */
    public static List<MavenReporterDescriptor> getConfigurableList() {
        List<MavenReporterDescriptor> r = new ArrayList<MavenReporterDescriptor>();
        for (MavenReporterDescriptor d : MavenReporterDescriptor.all()) {
            if(d.hasConfigScreen())
                r.add(d);
        }
        return r;
    }
}
