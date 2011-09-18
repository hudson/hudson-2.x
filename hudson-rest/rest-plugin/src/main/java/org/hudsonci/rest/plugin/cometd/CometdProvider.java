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

package org.hudsonci.rest.plugin.cometd;

import org.hudsonci.servlets.ServletContainer;
import org.hudsonci.servlets.ServletContainerAware;
import org.hudsonci.servlets.ServletRegistration;
import org.cometd.Bayeux;
import org.cometd.Channel;
import org.cometd.server.continuation.ContinuationCometdServlet;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.hudsonci.rest.plugin.ApiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Configures support for Bayeux via CometD.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class CometdProvider
    extends ApiProvider
{
    private static final Logger log = LoggerFactory.getLogger(CometdProvider.class);

    private static ServletRegistration.Handle handle;

    private static Bayeux bayeux;

    public static Bayeux getBayeux() {
        checkState(bayeux != null);
        return bayeux;
    }

    @Override
    public boolean isEnabled() {
        return handle != null && handle.isEnabled();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (handle != null) {
            handle.setEnabled(enabled);
        }
    }

    @Override
    public String toString() {
        return "Bayeux (CometD)";
    }

    public static Channel getChannel(final String channel, final boolean create) {
        if (bayeux != null) {
            try {
                return bayeux.getChannel(channel, create);
            }
            catch (IllegalStateException e) {
                log.warn("Failed to get channel", e);
            }
        }
        return null;
    }

    @Named
    @Singleton
    public static class ServletInstaller
        implements ServletContainerAware
    {
        private final ContinuationCometdServlet servlet = new ContinuationCometdServlet() {
            @Override
            public void init() throws ServletException {
                super.init();

                Bayeux b = servlet.getBayeux();
                b.addExtension(new AcknowledgedMessagesExtension());
                bayeux = b;
            }
        };

        public void setServletContainer(final ServletContainer container) throws Exception {
            assert container != null;

            log.debug("Installing CometD servlet");

            ServletRegistration reg = new ServletRegistration();
            reg.setServlet(servlet);
            reg.setUriPrefix("cometd");
            reg.addParameter("timeout", "60000");
            handle = container.register(reg);
        }
    }
}
