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
*    Kohsuke Kawaguchi, Seiji Sogabe, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.model.Descriptor.FormException;
import hudson.Extension;

/**
 * {@link View} that only contains projects for which the current user has access to.
 *
 * @since 1.220
 * @author Tom Huybrechts
 */
public class MyView extends View {
    @DataBoundConstructor
    public MyView(String name) {
        super(name);
    }

    public MyView(String name, ViewGroup owner) {
        this(name);
        this.owner = owner;
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return item.hasPermission(Job.CONFIGURE);
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
        return Hudson.getInstance().doCreateItem(req, rsp);
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        List<TopLevelItem> items = new ArrayList<TopLevelItem>();
        for (TopLevelItem item : Hudson.getInstance().getItems()) {
            if (item.hasPermission(Job.CONFIGURE)) {
                items.add(item);
            }
        }
        return Collections.unmodifiableList(items);
    }

    @Override
    public String getPostConstructLandingPage() {
        return ""; // there's no configuration page
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        // noop
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {
        // noop
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {
        /**
         * If the security is not enabled, there's no point in having
         * this type of views.
         */
        @Override
        public boolean isInstantiable() {
            return Hudson.getInstance().isUseSecurity();
        }

        public String getDisplayName() {
            return Messages.MyView_DisplayName();
        }
    }
}
