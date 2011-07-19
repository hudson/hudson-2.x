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

package hudson.maven;

import hudson.ExtensionPoint;
import hudson.ExtensionList;
import hudson.Extension;
import hudson.model.Hudson;
import org.apache.maven.project.MavenProject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.hudson.legacy.maven.plugin.ModuleDependency;

import org.eclipse.hudson.legacy.maven.plugin.PomInfo; 

/**
 * Extension point in Hudson to find additional dependencies from {@link MavenProject}.
 *
 * <p>
 * Maven plugin configurations often have additional configuration entries to specify
 * artifacts that a build depends on. Plugins can contribute an implementation of
 * this interface to find such hidden dependencies.
 *
 * <p>
 * To register implementations, put {@link Extension} on your subclass.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.264
 * @see HUDSON-2685
 */
public abstract class ModuleDependencyLocator implements ExtensionPoint {
    /**
     * Discovers hidden dependencies.
     *
     * @param project
     *      In memory representation of Maven project, from which the hidden dependencies will be extracted.
     *      Never null.
     * @param pomInfo
     *      Partially filled {@link PomInfo} object. Dependencies returned from this method will be
     *      added to this object by the caller.
     */
    public abstract Collection<ModuleDependency> find(MavenProject project, PomInfo pomInfo);

    /**
     * Returns all the registered {@link ModuleDependencyLocator} descriptors.
     */
    public static ExtensionList<ModuleDependencyLocator> all() {
        return Hudson.getInstance().getExtensionList(ModuleDependencyLocator.class);
    }

    /**
     * Facade of {@link ModuleDependencyLocator}.
     */
    /*package*/ static class ModuleDependencyLocatorFacade extends ModuleDependencyLocator {
        @Override
        public Collection<ModuleDependency> find(MavenProject project, PomInfo pomInfo) {
            Set<ModuleDependency> r = new HashSet<ModuleDependency>();
            for (ModuleDependencyLocator m : all())
                r.addAll(m.find(project,pomInfo));
            return r;
        }
    }
}
