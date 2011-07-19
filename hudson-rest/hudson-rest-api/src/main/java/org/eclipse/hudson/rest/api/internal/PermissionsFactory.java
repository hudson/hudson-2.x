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

package org.eclipse.hudson.rest.api.internal;

import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import org.eclipse.hudson.rest.model.PermissionsDTO;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates permission-set from {@link AccessControlled} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PermissionsFactory
{
    private final PermissionConverter permx;

    @Inject
    PermissionsFactory(final PermissionConverter permx) {
        this.permx = checkNotNull(permx);
    }

    public PermissionsDTO create(final AccessControlled target) {
        assert target != null;

        PermissionsDTO perms = new PermissionsDTO();

        for (Permission perm : findAllPermissions()) {
            if (target.hasPermission(perm)) {
                perms.getPermissions().add(permx.convert(perm));
            }
        }

        return perms;
    }

    private List<Permission> findAllPermissions() {
        List<Permission> perms = new ArrayList<Permission>();

        for (PermissionGroup group : PermissionGroup.getAll()) {
            perms.addAll(group.getPermissions());
        }

        return perms;
    }
}
