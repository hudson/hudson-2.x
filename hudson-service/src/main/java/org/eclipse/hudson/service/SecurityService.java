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

package org.eclipse.hudson.service;

import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.security.Permission;

import java.util.concurrent.Callable;

import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.eclipse.hudson.service.internal.SecurityServiceImpl;

import com.google.inject.ImplementedBy;

/**
 * Security services.
 *
 * @since 2.1.0
 */
@ImplementedBy(SecurityServiceImpl.class)
public interface SecurityService {
    /**
     * Check a Permission against {@link hudson.model.Hudson} for the current
     * user.
     *
     * @see hudson.model.Hudson#checkPermission(Permission)
     * @param permission the permission to check
     * @throws AccessDeniedException if access for the given permission is
     * denied
     */
    void checkPermission(Permission permission);

    /**
     * Check a Permission against an {@link AccessControlled} object in the current
     * security context.
     * <p>
     * Recommended to use this instead of checking permission on the object
     * directly. Consider this method a funnel for access security.
     *
     * @param controlled the instance under control
     * @param permission the permission to check on the access controlled object
     */
    void checkPermission(AccessControlled controlled, Permission permission);

    /**
     * Check if a an {@link AccessControlled} instance will allow the current
     * security context the specified {@link Permission}.
     * <p>
     * Recommended to use this instead of checking has permission on the object
     * directly. Consider this method a funnel for access security.
     *
     * @param controlled the instance under control
     * @param permission the permission to check on the access controlled object
     * @return true if current security context has the specified permission
     */
    boolean hasPermission(AccessControlled controlled, Permission permission);

    User getCurrentUser();

    User getUser(String id);

    User getUnknownUser();

    void runAs(Authentication auth, Runnable task);

    <T> T callAs(Authentication auth, Callable<T> task) throws Exception;

    <T> T callAs2(Authentication auth, Callable<T> task);
}
