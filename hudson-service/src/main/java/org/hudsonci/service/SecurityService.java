/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.service;

import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.security.Permission;

import java.util.concurrent.Callable;

import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.hudsonci.service.internal.SecurityServiceImpl;

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
