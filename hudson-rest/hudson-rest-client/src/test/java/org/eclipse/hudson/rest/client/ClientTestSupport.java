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

package org.eclipse.hudson.rest.client;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.eclipse.hudson.rest.client.HudsonClient;
import org.eclipse.hudson.rest.client.OpenOptions;
import org.eclipse.hudson.rest.client.internal.HudsonClientModule;
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
