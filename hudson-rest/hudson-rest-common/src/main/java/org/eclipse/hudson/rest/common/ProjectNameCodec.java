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

package org.eclipse.hudson.rest.common;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Helper to encode/decode project names.
 *
 * This is a work around since {@literal '/'} causes problems with Apache HTTPD reverse proxy, so
 * its translated to {@literal '@'}, which must be decoded before being used internally to
 * lookup projects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ProjectNameCodec
{
    public String encode(final String projectName) {
        try {
            return URLEncoder.encode(projectName.replace('/', '@'), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // This should never happen
            throw new Error(e);
        }
    }

    public String decode(final String projectName) {
        try {
            return URLDecoder.decode(projectName, "UTF-8").replace('@','/');
        }
        catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new Error(e);
        }
    }
}
