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

package org.eclipse.hudson.rest.plugin.cometd;

import org.cometd.Bayeux;
import org.cometd.Channel;
import org.cometd.server.continuation.ContinuationCometdServlet;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.eclipse.hudson.rest.plugin.ApiProvider;
import org.eclipse.hudson.servlets.ServletContainer;
import org.eclipse.hudson.servlets.ServletContainerAware;
import org.eclipse.hudson.servlets.ServletRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

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
        private final ContinuationCometdServlet servlet = new ContinuationCometdServlet();

        public void setServletContainer(final ServletContainer container) throws Exception {
            assert container != null;

            log.debug("Installing CometD servlet");

            ServletRegistration reg = new ServletRegistration();
            reg.setServlet(servlet);
            reg.setUriPrefix("cometd");
            reg.addParameter("timeout", "60000");
            handle = container.register(reg);

            Bayeux b = servlet.getBayeux();
            b.addExtension(new AcknowledgedMessagesExtension());
            bayeux = b;
        }
    }
}
