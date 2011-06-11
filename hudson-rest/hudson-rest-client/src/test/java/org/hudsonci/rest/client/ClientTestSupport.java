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

package org.hudsonci.rest.client;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.hudsonci.rest.client.HudsonClient;
import org.hudsonci.rest.client.OpenOptions;
import org.hudsonci.rest.client.internal.HudsonClientModule;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Support for client tests.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class ClientTestSupport
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private DefaultBeanLocator container;

    private HudsonClient client;

    public HudsonClient getClient() {
        return client;
    }

    @Before
    public void setUp() throws Exception {
        container = new DefaultBeanLocator();
        log.debug("Container: {}", container);

        client = createClient();
        log.debug("Client: {}", client);
    }

    protected HudsonClient createClient() {
        List<Module> modules = new ArrayList<Module>();

        modules.add(new Module()
        {
            public void configure(Binder binder) {
                binder.bind(MutableBeanLocator.class).toInstance(container);
            }
        });

        modules.add(new HudsonClientModule());

        configureModules(modules);

        Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new WireModule(modules));
        container.add(injector, 0);

        return injector.getInstance(HudsonClient.class);
    }

    protected void configureModules(final List<Module> modules) {
        assert modules != null;
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
        if (container != null) {
            container.clear();
            container = null;
        }
    }

    protected URI getServerUri() throws URISyntaxException {
        String uri = System.getProperty("hudson.uri", "http://localhost:8080");
        return new URI(uri);
    }

    protected OpenOptions getOpenOptions() {
        return new OpenOptions()
            .setRetries(OpenOptions.UNLIMITED_RETRIES)
            .setTimeout(5 * 60); // timeout after 5 minutes
    }

    protected void open() throws Exception {
        HudsonClient client = getClient();
        assertFalse(getClient().isOpen());

        client.open(getServerUri(), getOpenOptions());
        assertTrue(getClient().isOpen());
    }
}
