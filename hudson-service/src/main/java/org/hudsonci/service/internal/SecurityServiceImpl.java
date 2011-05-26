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


package org.hudsonci.service.internal;

import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.hudsonci.service.SecurityService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SecurityService} implementation.
 *
 * @author plynch
 * @since 2.1.0
 */
@Named
@Singleton
public class SecurityServiceImpl
    extends ServiceSupport
    implements SecurityService
{
    @Inject
    SecurityServiceImpl() {
        super();
    }

    public void checkPermission(final Permission permission) {
        getHudson().checkPermission(permission);
    }

    public User getCurrentUser() {
        return User.current();
    }

    public User getUser(final String id) {
        // id may be null
        return getHudson().getUser(id);
    }

    public User getUnknownUser() {
        return User.getUnknown();
    }

    public void runAs(final Authentication auth, final Runnable task) {
        checkNotNull(auth);
        checkNotNull(task);
        final SecurityContext ctx = SecurityContextHolder.getContext();
        final Authentication current = ctx.getAuthentication();
        ctx.setAuthentication(auth);
        try {
            task.run();
        }
        finally {
            ctx.setAuthentication(current);
        }
    }

    public <T> T callAs(final Authentication auth, final Callable<T> task) throws Exception {
        checkNotNull(auth);
        checkNotNull(task);
        final SecurityContext ctx = SecurityContextHolder.getContext();
        final Authentication current = ctx.getAuthentication();
        ctx.setAuthentication(auth);
        try {
            return task.call();
        }
        finally {
            ctx.setAuthentication(current);
        }
    }

    public <T> T callAs2(final Authentication auth, final Callable<T> task) {
        try {
            return callAs(auth, task);
        }
        catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    public void checkPermission(final AccessControlled controlled, final Permission permission) {
        checkNotNull(controlled);
        checkNotNull(permission);
        controlled.checkPermission(permission);
    }

    public boolean hasPermission(final AccessControlled controlled, final Permission permission) {
        checkNotNull(controlled);
        checkNotNull(permission);
        return controlled.hasPermission(permission);
    }
}
