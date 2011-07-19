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
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A view that delegates to another.
 * 
 * TODO: this does not respond to renaming or deleting the proxied view.
 * 
 * @author Tom Huybrechts
 *
 */
public class ProxyView extends View implements StaplerFallback {

    private String proxiedViewName;

    @DataBoundConstructor
    public ProxyView(String name) {
        super(name);

        if (Hudson.getInstance().getView(name) != null) {
            // if this is a valid global view name, let's assume the
            // user wants to show it
            proxiedViewName = name;
        }
    }

    public View getProxiedView() {
        if (proxiedViewName == null) {
            // just so we avoid errors just after creation
            return Hudson.getInstance().getPrimaryView();
        } else {
            return Hudson.getInstance().getView(proxiedViewName);
        }
    }

    public String getProxiedViewName() {
        return proxiedViewName;
    }

    public void setProxiedViewName(String proxiedViewName) {
        this.proxiedViewName = proxiedViewName;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return getProxiedView().getItems();
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return getProxiedView().contains(item);
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        if (oldName.equals(proxiedViewName)) {
            proxiedViewName = newName;
        }
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {
        String proxiedViewName = req.getSubmittedForm().getString("proxiedViewName");
        if (Hudson.getInstance().getView(proxiedViewName) == null) {
            throw new FormException("Not an existing global view", "proxiedViewName");
        }
        this.proxiedViewName = proxiedViewName;
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return getProxiedView().doCreateItem(req, rsp);
    }

    /**
     * Fails if a global view with the given name does not exist.
     */
    public FormValidation doViewExistsCheck(@QueryParameter String value) {
        checkPermission(View.CREATE);

        String view = Util.fixEmpty(value);
        if(view==null) return FormValidation.ok();

        if(Hudson.getInstance().getView(view)!=null)
            return FormValidation.ok();
        else
            return FormValidation.error(Messages.ProxyView_NoSuchViewExists(value));
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.ProxyView_DisplayName();
        }
        
        @Override
        public boolean isInstantiable() {
        	// doesn't make sense to add a ProxyView to the global views
        	return !(Stapler.getCurrentRequest().findAncestorObject(ViewGroup.class) instanceof Hudson);
        }

    }

    public Object getStaplerFallback() {
        return getProxiedView();
    }

}
