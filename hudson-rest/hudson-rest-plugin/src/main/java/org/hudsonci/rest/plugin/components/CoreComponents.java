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

package org.hudsonci.rest.plugin.components;

import org.hudsonci.rest.api.internal.FaultExceptionMapper;
import org.hudsonci.rest.api.internal.GenericExceptionMapper;
import org.hudsonci.rest.api.internal.AcegiSecurityExceptionMapper;
import org.hudsonci.rest.api.internal.HandshakeResource;
import org.hudsonci.rest.api.internal.NotFoundExceptionMapper;
import org.hudsonci.rest.api.internal.WebApplicationExceptionMapper;
import org.hudsonci.rest.api.status.StatusResource;
import org.hudsonci.rest.common.JacksonProvider;

import javax.inject.Named;
import javax.inject.Singleton;

import org.hudsonci.rest.plugin.RestComponentProvider;

/**
 * Provides the core components required by the REST subsystem.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class CoreComponents
    extends RestComponentProvider
{
    @Override
    public Class<?>[] getClasses() {
        return new Class[] {
            JacksonProvider.class,
            FaultExceptionMapper.class,
            NotFoundExceptionMapper.class,
            AcegiSecurityExceptionMapper.class,
            WebApplicationExceptionMapper.class,
            GenericExceptionMapper.class,

            // These are not really core, but specific to the handshake
            HandshakeResource.class,
            StatusResource.class,
        };
    }
}
