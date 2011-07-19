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

package org.eclipse.hudson.rest.client.internal.jersey;

import org.eclipse.hudson.utils.io.PrintBuffer;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Filter which logs request and response details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class LoggingFilter
    extends ClientFilter
{
    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private final Logger logger;

    private final boolean trace;

    private final AtomicLong counter = new AtomicLong(0);

    public LoggingFilter(final Logger logger, final boolean trace) {
        this.logger = checkNotNull(logger);
        this.trace = trace;
    }

    public LoggingFilter(final Logger logger) {
        this(logger, false);
    }

    public LoggingFilter() {
        this(LoggerFactory.getLogger(LoggingFilter.class));
    }

    @Override
    public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {
        checkNotNull(request);

        final boolean enabled = trace ? logger.isTraceEnabled() : logger.isDebugEnabled();
        final Long id;

        if (enabled) {
            id = counter.incrementAndGet();
            logRequest(id, request);
        }
        else {
            id = null;
        }

        final ClientResponse response = getNext().handle(request);

        if (enabled) {
            try {
                logResponse(id, response);
            }
            catch (IOException e) {
                throw new ClientHandlerException(e);
            }
        }

        return response;
    }

    private void log(final PrintBuffer buff) {
        if (trace) {
            logger.trace("\n{}", buff);
        }
        else {
            logger.debug("\n{}", buff);
        }
    }

    private PrintBuffer prefixId(final PrintBuffer buff, final long id) {
        buff.print(id);
        buff.print(" ");
        return buff;
    }

    private void logHeaders(final PrintBuffer buff, final long id, final String prefix, final MultivaluedMap<String, ?> headers) {
        for (Entry<String, ? extends List<?>> e : headers.entrySet()) {
            String header = e.getKey();
            for (Object value : e.getValue()) {
                prefixId(buff, id).append(prefix).append(header).append(": ").println(value);
            }
        }
    }

    private void logProperties(final PrintBuffer buff, final long id, final String prefix, final Map<String, Object> props) {
        for (Map.Entry<String,Object> entry : props.entrySet()) {
            prefixId(buff, id).append(prefix).append(entry.getKey()).append("=").println(entry.getValue());
        }
    }

    private void logRequest(final long id, final ClientRequest request) {
        assert request != null;

        PrintBuffer buff = new PrintBuffer();

        prefixId(buff, id).append(NOTIFICATION_PREFIX).println("Client out-bound request");
        prefixId(buff, id).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").println(request.getURI().toASCIIString());

        logHeaders(buff, id, REQUEST_PREFIX, request.getHeaders());
        logProperties(buff, id, REQUEST_PREFIX, request.getProperties());

        // Log the entity if there is one
        Object entity = request.getEntity();
        if (entity != null) {
            prefixId(buff, id).println(REQUEST_PREFIX);
            buff.println(entity);

            // Reset the entity for upstream consumers
            request.setEntity(entity);
        }

        log(buff);
    }

    private void logResponse(final long id, final ClientResponse response) throws IOException {
        assert response != null;

        PrintBuffer buff = new PrintBuffer();

        prefixId(buff, id).append(NOTIFICATION_PREFIX).println("Client in-bound response");
        prefixId(buff, id).append(RESPONSE_PREFIX).println(response.getStatus());

        logHeaders(buff, id, RESPONSE_PREFIX, response.getHeaders());
        logProperties(buff, id, RESPONSE_PREFIX, response.getProperties());

        // Log the entity if there is one
        if (response.hasEntity()) {
            prefixId(buff, id).println(RESPONSE_PREFIX);

            InputStream input = response.getEntityInputStream();
            assert input != null;

            // Copy the entity to a buffer so we can reset it
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            copy(input, output);
            byte[] bytes = output.toByteArray();

            // Log the contents
            copy(new InputStreamReader(new ByteArrayInputStream(bytes)), buff);

            // Reset the entity for downstream consumers
            response.setEntityInputStream(new ByteArrayInputStream(bytes));
        }

        log(buff);
    }

    //
    // IO Helpers
    //

    private static final int BUFFER_SIZE = 1024 * 4;

    private static void copy(final Reader input, final Writer output) throws IOException {
        final char[] buffer = new char[BUFFER_SIZE];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        output.flush();
    }

    private static void copy(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        output.flush();
    }
}
