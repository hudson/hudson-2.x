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

package org.eclipse.hudson.utils.plugin.ui;

import org.eclipse.hudson.inject.internal.plugin.PluginClassLoader;

import hudson.PluginWrapper;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.security.Permission;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Support for UI components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class UIComponentSupport<P extends Action>
    implements Action
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final P parent;

    protected UIComponentSupport(final P parent) {
        this.parent = parent;
    }

    protected UIComponentSupport() {
        this(null);
    }

    public P getParent() {
        return parent;
    }

    /**
     * Returns our parent's icon.
     */
    public String getIconFileName() {
        if (parent != null) {
            return getParent().getIconFileName();
        }
        return null;
    }

//    @Override
//    public String getDisplayName() {
//        if (parent != null) {
//            return getParent().getDisplayName();
//        }
//        return null;
//    }

    protected String getIconFileName(final String name) {
        assert name != null;
        // This path is relative to the context path, not root
        return String.format("/plugin/%s/images/%s", getPluginName(), name);
    }

    /**
     * No URL.
     */
    public String getUrlName() {
        return null;
    }

    // FIXME: Sync up with PluginUtil helpers

    /**
     * Returns the {@link PluginWrapper} for the current class context.
     *
     * @throws IllegalStateException Unable to determine plugin wrapper source.
     */
    protected PluginWrapper getPluginWrapper() {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof PluginClassLoader) {
            return ((PluginClassLoader)cl).getPlugin();
        }
        throw new IllegalStateException();
    }

    @JellyAccessible
    public String getRootPath() {
        StaplerRequest req = Stapler.getCurrentRequest();
        checkState(req != null, "StaplerRequest not bound");
        return req.getContextPath();
    }

    @JellyAccessible
    public String getPluginName() {
        return getPluginWrapper().getShortName();
    }

    @JellyAccessible
    public String getPluginPath() {
        return String.format("%s/plugin/%s", getRootPath(), getPluginName());
    }

    @JellyAccessible
    public String getImagesPath() {
        return String.format("%s/images", getPluginPath());
    }

    @JellyAccessible
    public String getHelpPath() {
        // Help path is relative to the context path, not root
        return String.format("/plugin/%s/help", getPluginName());
    }

    @JellyAccessible
    public String getIconPath() {
        String iconPath = getIconFileName();
        if (iconPath.startsWith("/")) {
            iconPath = iconPath.substring(1, iconPath.length());
        }
        return String.format("%s/%s", getRootPath(), iconPath);
    }

    /**
     * The object which owns the <tt>sidepanel.jelly</tt> view.
     */
    @JellyAccessible
    public Object getSidePanelOwner() {
        if (parent instanceof UIComponentSupport) {
            return ((UIComponentSupport)parent).getSidePanelOwner();
        }
        return parent;
    }

    //
    // TODO: Add getBaseUrl() which should end up similar to getSidePanelOwner().getUrlName()
    //
    
    /**
     * The permission needed to render the components view.
     */
    @JellyAccessible
    public Permission getViewPermission() {
        return Hudson.READ;
    }
    
    @JellyAccessible
    public String getPageTitle() {
        return getDisplayName();
    }

    protected void checkPermission(final Permission perm) {
        // TODO: inject
        Hudson.getInstance().checkPermission(perm);
    }

    protected void redirect(final StaplerRequest req, final StaplerResponse resp, final String location) throws IOException {
        log.trace("Redirecting to: {}", location);
        resp.sendRedirect(location);
    }

    protected void redirectAncestor(final StaplerRequest req, final StaplerResponse resp, final Class type) throws IOException {
        log.trace("Redirect ancestor: {}", type);
        redirect(req, resp, req.findAncestor(type).getUrl());
    }

    protected void redirectAncestor(final StaplerRequest req, final StaplerResponse resp, final Object obj) throws IOException {
        log.trace("Redirect ancestor: {}", obj);
        Ancestor ancestor = req.findAncestor(obj);
        redirect(req, resp, ancestor.getUrl());
    }

    protected void redirectParent(final StaplerRequest req, final StaplerResponse resp) throws IOException {
        redirectAncestor(req, resp, getParent());
    }

    protected void redirectSelf(final StaplerRequest req, final StaplerResponse resp) throws IOException {
        redirectAncestor(req, resp, this);
    }

    @JellyAccessible
    public String getBaseRestURI() {
        return String.format("%s/rest/plugin/%s", getRootPath(), getPluginName());
    }
}
