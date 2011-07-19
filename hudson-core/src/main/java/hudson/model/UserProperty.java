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

import hudson.ExtensionPoint;
import hudson.Plugin;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor.FormException;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Extensible property of {@link User}.
 *
 * <p>
 * {@link Plugin}s can extend this to define custom properties
 * for {@link User}s. {@link UserProperty}s show up in the user
 * configuration screen, and they are persisted with the user object.
 *
 * <p>
 * Configuration screen should be defined in <tt>config.jelly</tt>.
 * Within this page, the {@link UserProperty} instance is available
 * as <tt>instance</tt> variable (while <tt>it</tt> refers to {@link User}.
 * See {@link Mailer.UserProperty}'s <tt>config.jelly</tt> for an example.
 *
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class UserProperty implements Describable<UserProperty>, ExtensionPoint {
    /**
     * The user object that owns this property.
     * This value will be set by the Hudson code.
     * Derived classes can expect this value to be always set.
     */
    protected transient User user;

    /*package*/ final void setUser(User u) {
        this.user = u;
    }

    // descriptor must be of the UserPropertyDescriptor type
    public UserPropertyDescriptor getDescriptor() {
        return (UserPropertyDescriptor)Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Returns all the registered {@link UserPropertyDescriptor}s.
     */
    public static DescriptorExtensionList<UserProperty,UserPropertyDescriptor> all() {
        return Hudson.getInstance().<UserProperty,UserPropertyDescriptor>getDescriptorList(UserProperty.class);
    }

    public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws FormException {
    	return getDescriptor().newInstance(req, form);
    }
}
