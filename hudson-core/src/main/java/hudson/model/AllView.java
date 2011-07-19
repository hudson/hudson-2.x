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
*    Kohsuke Kawaguchi, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;

import hudson.model.Descriptor.FormException;
import hudson.Extension;

/**
 * {@link View} that contains everything.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.269
 */
public class AllView extends View {
    @DataBoundConstructor
    public AllView(String name) {
        super(name);
    }

    public AllView(String name, ViewGroup owner) {
        this(name);
        this.owner = owner;
    }
    
    @Override
    public String getDescription() {
        return Hudson.getInstance().getDescription();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return true;
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
        return Hudson.getInstance().doCreateItem(req, rsp);
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return Hudson.getInstance().getItems();
    }

    @Override
    public synchronized void doSubmitDescription( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        checkPermission(Hudson.ADMINISTER);

        Hudson.getInstance().setSystemMessage(req.getParameter("description"));
        rsp.sendRedirect(".");
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
        @Override
        public boolean isInstantiable() {
            for (View v : Stapler.getCurrentRequest().findAncestorObject(ViewGroup.class).getViews())
                if(v instanceof AllView)
                    return false;
            return true;
        }

        public String getDisplayName() {
            return Messages.Hudson_ViewName();
        }
    }
}
