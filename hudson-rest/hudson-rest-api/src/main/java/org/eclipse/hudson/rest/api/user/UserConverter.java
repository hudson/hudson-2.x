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

package org.eclipse.hudson.rest.api.user;


import org.eclipse.hudson.rest.api.internal.ConverterSupport;
import org.eclipse.hudson.rest.model.UserDTO;

/**
 * Converts {@link hudson.model.User} into {@link UserDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class UserConverter
    extends ConverterSupport
{
    public UserDTO convert(final hudson.model.User source) {
        assert source != null;

        log.trace("Converting: {}", source);

        UserDTO target = new UserDTO();
        target.setId(source.getId());
        target.setFullName(source.getFullName());
        target.setDescription(source.getDescription());

        return target;
    }
}
