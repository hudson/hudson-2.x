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

package hudson.model;

import hudson.ExtensionList;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link Descriptor} for {@link TopLevelItem}s.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TopLevelItemDescriptor extends Descriptor<TopLevelItem> {
    protected TopLevelItemDescriptor(Class<? extends TopLevelItem> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link TopLevelItem} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected TopLevelItemDescriptor() {
    }

    /**
     * {@link TopLevelItemDescriptor}s often uses other descriptors to decorate itself.
     * This method allows the subtype of {@link TopLevelItemDescriptor}s to filter them out.
     *
     * <p>
     * This is useful for a workflow/company specific job type that wants to eliminate
     * options that the user would see.
     *
     * @since 1.294
     */
    public boolean isApplicable(Descriptor descriptor) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Used as the caption when the user chooses what job type to create.
     * The descriptor implementation also needs to have <tt>newJobDetail.jelly</tt>
     * script, which will be used to render the text below the caption
     * that explains the job type.
     */
    public abstract String getDisplayName();

    /**
     * @deprecated since 2007-01-19.
     *      This is not a valid operation for {@link Job}s.
     */
    @Deprecated
    public TopLevelItem newInstance(StaplerRequest req) throws FormException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link TopLevelItem}.
     *
     * @deprecated as of 1.390
     *      Use {@link #newInstance(ItemGroup, String)}
     */
    public TopLevelItem newInstance(String name) {
        return newInstance(Hudson.getInstance(), name);
    }

    /**
     * Creates a new {@link TopLevelItem} for the specified parent.
     *
     * @since 1.390
     */
    public abstract TopLevelItem newInstance(ItemGroup parent, String name);

    /**
     * Returns all the registered {@link TopLevelItem} descriptors.
     */
    public static ExtensionList<TopLevelItemDescriptor> all() {
        return Items.all();
    }

}
