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

package org.hudsonci.rest.plugin;

import org.hudsonci.events.EventConsumer;
import org.hudsonci.events.ready.ReadyEvent;

import hudson.Plugin;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.EventObject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hudson REST plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class RestPlugin
    extends Plugin
{
    private static final Logger log = LoggerFactory.getLogger(RestPlugin.class);

    private transient List<ApiProvider> providers;

    private boolean enabled = true;

    @Inject
    public RestPlugin(final List<ApiProvider> providers) {
        this.providers = checkNotNull(providers);

        log.debug("API version: {}", org.hudsonci.rest.api.Version.get());

        if (log.isDebugEnabled()) {
            log.debug("Providers:");
            for (ApiProvider provider : providers) {
                log.debug("    {}", provider);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        enable();
    }

    /**
     * Enable or disable providers based on {@link #enabled} flag.
     */
    public void enable() {
        for (ApiProvider provider : providers) {
            provider.setEnabled(enabled);
            log.info("API provider {} {}", provider, enabled ? "enabled" : "disabled");
        }
    }

    @Override
    public void start() throws Exception {
        load();
    }

    @Override
    public void postInitialize() throws Exception {
        log.debug("Starting providers");

        for (ApiProvider provider : providers) {
            provider.start();
        }
    }

    @Named
    @Singleton
    public static class ProviderEnabler
        implements EventConsumer
    {
        private final RestPlugin plugin;

        @Inject
        public ProviderEnabler(final RestPlugin plugin) {
            this.plugin = checkNotNull(plugin);
        }

        public void consume(final EventObject event) throws Exception {
            if (event instanceof ReadyEvent) {
                plugin.enable();
            }
        }
    }

    @Override
    public void stop() throws Exception {
        log.debug("Stopping providers");

        for (ApiProvider provider : providers) {
            provider.stop();
        }
    }

    @Override
    public void configure(final StaplerRequest request, final JSONObject data)
        throws IOException, ServletException, Descriptor.FormException
    {
        checkNotNull(data);
        boolean prev = isEnabled();

        setEnabled(data.optBoolean("enabled", true));
        if (prev != isEnabled()) {
            log.debug("API {}", enabled ? "enabled" : "disabled");
        }
        
        save();
    }
}
