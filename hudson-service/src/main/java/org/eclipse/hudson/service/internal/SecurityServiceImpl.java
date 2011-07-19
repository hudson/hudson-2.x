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

package org.eclipse.hudson.service.internal;

import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.eclipse.hudson.service.SecurityService;

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
