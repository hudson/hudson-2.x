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

import org.eclipse.hudson.rest.client.HandshakeFailedException;
import org.eclipse.hudson.rest.client.HudsonClient;
import org.eclipse.hudson.rest.client.ext.StatusClient;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Test for client creation and basic extension access.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CreateClientTest
    extends ClientTestSupport
{
    @Test
    public void testCreate() {
        HudsonClient client = getClient();
        assertNotNull(client);

        StatusClient statusx = client.ext(StatusClient.class);
        assertNotNull(statusx);
    }

    @Test(expected=HandshakeFailedException.class)
    public void testCreateWithOpen() throws URISyntaxException {
        HudsonClient client = getClient();
        assertNotNull(client);

        // Try to open, this will trigger loading/wiring of the related client bits
        client.open(new URI("http://localhost:12345"));
    }
}
