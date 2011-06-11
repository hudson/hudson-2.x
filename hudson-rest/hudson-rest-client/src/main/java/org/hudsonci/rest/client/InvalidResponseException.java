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

import com.sun.jersey.api.client.ClientResponse;

/**
 * Thrown to indicate that a response is not valid or unexpected.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class InvalidResponseException
    extends HudsonClientException
{
    private final ClientResponse response;

    public InvalidResponseException(final ClientResponse response, final String message, final Throwable cause) {
        super(message, cause);
        assert response != null;
        this.response = response;
    }

    public InvalidResponseException(final ClientResponse response, final String message) {
        this(response, message, null);
    }

    public InvalidResponseException(final ClientResponse response, final Throwable cause) {
        this(response, null, cause);
    }

    public InvalidResponseException(final ClientResponse response) {
        this(response, null, null);
    }

    public ClientResponse getResponse() {
        return response;
    }

    @Override
    public String getMessage() {
        StringBuilder buff = new StringBuilder();
        
        String msg = super.getMessage();
        if (msg != null) {
            buff.append(msg);
            buff.append(" ");
        }
        
        buff.append("[");
        buff.append(response.getStatus());
        buff.append("] ");
        buff.append(response.getClientResponseStatus().getReasonPhrase());

        return buff.toString();
    }
}
