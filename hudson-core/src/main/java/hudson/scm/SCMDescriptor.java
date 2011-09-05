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

package hudson.scm;

import hudson.model.Descriptor;
import hudson.model.AbstractProject;

import java.util.List;
import java.util.Collections;
import java.util.logging.Logger;
import static java.util.logging.Level.WARNING;
import java.lang.reflect.Field;

/**
 * {@link Descriptor} for {@link SCM}.
 *
 * @param <T>
 *      The 'self' type that represents the type of {@link SCM} that
 *      this descriptor describes.
 * @author Kohsuke Kawaguchi
 */
public abstract class SCMDescriptor<T extends SCM> extends Descriptor<SCM> {
    /**
     * If this SCM has corresponding {@link RepositoryBrowser},
     * that type. Otherwise this SCM will not have any repository browser.
     */
    public transient final Class<? extends RepositoryBrowser> repositoryBrowser;

    /**
     * Incremented every time a new {@link SCM} instance is created from this descriptor. 
     * This is used to invalidate cache. Due to the lack of synchronization and serialization,
     * this field doesn't really count the # of instances created to date,
     * but it's good enough for the cache invalidation.
     */
    //TODO: review and check whether we can do it private
    public volatile int generation = 1;

    protected SCMDescriptor(Class<T> clazz, Class<? extends RepositoryBrowser> repositoryBrowser) {
        super(clazz);
        this.repositoryBrowser = repositoryBrowser;
    }

    /**
     * Infers the type of the corresponding {@link SCM} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected SCMDescriptor(Class<? extends RepositoryBrowser> repositoryBrowser) {
        this.repositoryBrowser = repositoryBrowser;
    }

    public int getGeneration() {
        return generation;
    }

    // work around HUDSON-4514. The repositoryBrowser field was marked as non-transient until 1.325,
    // causing the field to be persisted and overwritten on the load method.
    @SuppressWarnings({"ConstantConditions"})
    @Override
    public void load() {
        Class<? extends RepositoryBrowser> rb = repositoryBrowser;
        super.load();
        if (repositoryBrowser!=rb) { // XStream may overwrite even the final field.
            try {
                Field f = SCMDescriptor.class.getDeclaredField("repositoryBrowser");
                f.setAccessible(true);
                f.set(this,rb);
            } catch (NoSuchFieldException e) {
                LOGGER.log(WARNING, "Failed to overwrite the repositoryBrowser field",e);
            } catch (IllegalAccessException e) {
                LOGGER.log(WARNING, "Failed to overwrite the repositoryBrowser field",e);
            }
        }
    }

    /**
     * Optional method used by the automatic SCM browser inference.
     *
     * <p>
     * Implementing this method allows Hudson to reuse {@link RepositoryBrowser}
     * configured for one project to be used for other "compatible" projects.
     * 
     * @return
     *      true if the two given SCM configurations are similar enough
     *      that they can reuse {@link RepositoryBrowser} between them.
     */
    public boolean isBrowserReusable(T x, T y) {
        return false;
    }

    /**
     * Allows {@link SCMDescriptor}s to choose which projects it wants to be configurable against.
     *
     * <p>
     * When this method returns false, this {@link SCM} will not appear in the configuration screen
     * for the given project. The default method always return true.
     *
     * @since 1.294
     */
    public boolean isApplicable(AbstractProject project) {
        return true;
    }

    /**
     * Returns the list of {@link RepositoryBrowser} {@link Descriptor}
     * that can be used with this SCM.
     *
     * @return
     *      can be empty but never null.
     */
    public List<Descriptor<RepositoryBrowser<?>>> getBrowserDescriptors() {
        if(repositoryBrowser==null)     return Collections.emptyList();
        return RepositoryBrowsers.filter(repositoryBrowser);
    }

    private static final Logger LOGGER = Logger.getLogger(SCMDescriptor.class.getName());
}
