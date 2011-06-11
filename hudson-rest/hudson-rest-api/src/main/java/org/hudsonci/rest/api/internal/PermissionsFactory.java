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

package org.hudsonci.rest.api.internal;

import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import org.hudsonci.rest.model.PermissionsDTO;

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
