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

import hudson.model.Hudson;
import hudson.model.Hudson.MasterComputer;
import hudson.remoting.VirtualChannel;
import hudson.util.RemotingDiagnostics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hudsonci.service.ScriptService;
import org.hudsonci.service.SecurityService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link ScriptService}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ScriptServiceImpl
    extends ServiceSupport
    implements ScriptService
{
    private final SecurityService security;

    @Inject
    public ScriptServiceImpl(final SecurityService security) {
        this.security = checkNotNull(security);
    }

    /**
     * {@inheritDoc}
     *
     * @see RemotingDiagnostics#executeGroovy(String, VirtualChannel)
     */
    public String execute(final String script) throws Exception {
        checkNotNull(script);
        security.checkPermission(Hudson.ADMINISTER);
        log.debug("Executing script on master: {}", script);
        return RemotingDiagnostics.executeGroovy(script, MasterComputer.localChannel);
    }
}
