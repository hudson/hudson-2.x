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

import hudson.model.Descriptor;
import hudson.model.Describable;
import hudson.model.Hudson;
import org.apache.commons.jelly.JellyException;
import org.eclipse.hudson.legacy.maven.plugin.reporters.MavenArtifactArchiver;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyClassTearOff;

import java.util.Collection;

/**
 * {@link Descriptor} for {@link MavenReporter}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class MavenReporterDescriptor extends Descriptor<MavenReporter> {
    protected MavenReporterDescriptor(Class<? extends MavenReporter> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link Describable} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected MavenReporterDescriptor() {
    }

    /**
     * Returns an instance used for automatic {@link MavenReporter} activation.
     *
     * <p>
     * Some {@link MavenReporter}s, such as {@link MavenArtifactArchiver},
     * can work just with the configuration in POM and don't need any additional
     * Hudson configuration. They also don't need any explicit enabling/disabling
     * as they can activate themselves by listening to the callback from the build
     * (for example javadoc archiver can do the work in response to the execution
     * of the javadoc target.)
     *
     * <p>
     * Those {@link MavenReporter}s should return a valid instance
     * from this method. Such instance will then participate into the build
     * and receive event callbacks.
     */
    public MavenReporter newAutoInstance(MavenModule module) {
        return null;
    }

    /**
     * If {@link #hasConfigScreen() the reporter has no configuration screen},
     * this method can safely return null, which is the default implementation.
     */
    @Deprecated
    public MavenReporter newInstance(StaplerRequest req) throws FormException {
        return null;
    }

    /**
     * Returns true if this descriptor has <tt>config.jelly</tt>.
     */
    public final boolean hasConfigScreen() {
        MetaClass c = WebApp.getCurrent().getMetaClass(getClass());
        try {
            JellyClassTearOff tearOff = c.loadTearOff(JellyClassTearOff.class);
            return tearOff.findScript(getConfigPage())!=null;
        } catch(JellyException e) {
            return false;
        }
    }

    /**
     * Lists all the currently registered instances of {@link MavenReporterDescriptor}.
     */
    public static Collection<MavenReporterDescriptor> all() {
        // use getDescriptorList and not getExtensionList to pick up legacy instances
        return Hudson.getInstance().<MavenReporter,MavenReporterDescriptor>getDescriptorList(MavenReporter.class);
    }
}
