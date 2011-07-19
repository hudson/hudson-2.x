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

package hudson.tasks;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.model.Hudson;
import hudson.model.User;

import java.util.List;

/**
 * Finds full name off the user when none is specified.
 *
 * <p>
 * This is an extension point of Hudson. Plugins tha contribute new implementation
 * of this class should use {@link Extension} to register the instance into Hudson, like this:
 *
 * <pre>
 * &#64;Extension
 * class MyserNameResolver extends UserNameResolver {
 *   ...
 * }
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 * @since 1.192
 */
public abstract class UserNameResolver implements ExtensionPoint {

    /**
     * Finds full name of the given user.
     *
     * <p>
     * This method is called when a {@link User} without explicitly name is used.
     *
     * <p>
     * When multiple resolvers are installed, they are consulted in order and
     * the search will be over when a name is found by someoene.
     *
     * <p>
     * Since {@link UserNameResolver} is singleton, this method can be invoked concurrently
     * from multiple threads.
     *
     * @return
     *      null if the inference failed.
     */
    public abstract String findNameFor(User u);
    
    public static String resolve(User u) {
        for (UserNameResolver r : all()) {
            String name = r.findNameFor(u);
            if(name!=null) return name;
        }

        return null;
    }

    /**
     * Returns all the registered {@link UserNameResolver} descriptors.
     */
    public static ExtensionList<UserNameResolver> all() {
        return Hudson.getInstance().getExtensionList(UserNameResolver.class);
    }

    /**
     * All registered {@link UserNameResolver} implementations.
     *
     * @deprecated since 2009-02-24.
     *      Use {@link #all()} for read access, and use {@link Extension} for registration.
     */
    public static final List<UserNameResolver> LIST = ExtensionListView.createList(UserNameResolver.class);
}
