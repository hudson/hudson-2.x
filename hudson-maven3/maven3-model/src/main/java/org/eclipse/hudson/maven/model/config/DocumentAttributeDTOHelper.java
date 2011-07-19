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

package org.eclipse.hudson.maven.model.config;

import org.eclipse.hudson.maven.model.config.DocumentAttributeDTO;

/**
 * Helper for {@link DocumentAttributeDTO}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DocumentAttributeDTOHelper
{
    public static String asString(final DocumentAttributeDTO source) {
        assert source != null;

        StringBuilder buff = new StringBuilder();
        buff.append(source.getName()).append("=");

        String value = source.getValue();
        if (value != null) {
            buff.append("'").append(value).append("'");
        }
        else {
            buff.append("null");
        }

        return buff.toString();
    }
}
