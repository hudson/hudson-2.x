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

import org.hudsonci.rest.client.HandshakeFailedException;
import org.hudsonci.rest.client.HudsonClient;
import org.hudsonci.rest.client.ext.StatusClient;
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
