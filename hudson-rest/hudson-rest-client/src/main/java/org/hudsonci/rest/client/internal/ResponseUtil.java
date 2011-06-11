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

package org.hudsonci.rest.client.internal;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;

import org.hudsonci.rest.client.InvalidResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;

/**
 * Response helpers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ResponseUtil
{
    private static final Logger log = LoggerFactory.getLogger(ResponseUtil.class);

    /**
     * Close the given response quietly.
     */
    public static void close(final ClientResponse response) {
        if (response != null) {
            try {
                response.close();
            }
            catch (ClientHandlerException e) {
                // For now leave as warn, not sure when/if this will happen yet
                log.warn("Failure while closing response", e);
            }
        }
    }

    public static StatusType ensureStatus(final ClientResponse response, final StatusType... expected) {
        assert response != null;
        assert expected != null && expected.length != 0;

        boolean invalid = true;
        StatusType status = response.getClientResponseStatus();

        for (StatusType expect : expected) {
            if (status.getStatusCode() == expect.getStatusCode()) {
                invalid = false;
                break;
            }
        }

        if (invalid) {
            throw new InvalidResponseException(response);
        }

        return status;
    }

    public static boolean isStatus(final ClientResponse response, final StatusType expected) {
        assert response != null;
        assert expected != null;
        return isStatus(response.getClientResponseStatus(), expected);
    }

    public static boolean isStatus(final StatusType status, final StatusType expected) {
        assert status != null;
        assert expected != null;
        return status.getStatusCode() == expected.getStatusCode();
    }

    /**
     * Check if the given response is compatible with <em>any</em> of the given media-types.
     */
    public static boolean isCompatible(final ClientResponse response, final MediaType... types) {
        assert response != null;
        assert types != null && types.length != 0;

        MediaType have = response.getType();
        for (MediaType type : types) {
            if (type.isCompatible(have)) {
                return true;
            }
        }

        return false;
    }
}
