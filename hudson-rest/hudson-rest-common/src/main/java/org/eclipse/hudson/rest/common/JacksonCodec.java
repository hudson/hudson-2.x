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

package org.eclipse.hudson.rest.common;

import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <a href="http://jackson.codehaus.org">Jackson</a> JSON codec.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class JacksonCodec
    implements JsonCodec
{
    private final ObjectMapper mapper;

    @Inject
    public JacksonCodec(final ObjectMapper mapper) {
        this.mapper = checkNotNull(mapper);
    }

    public JacksonCodec() {
        this(new ObjectMapperProvider().get());
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String encode(final Object value) throws IOException {
        checkNotNull(value);
        return mapper.writeValueAsString(value);
    }

    public <T> T decode(final String value, final Class<T> type) throws IOException {
        checkNotNull(value);
        checkNotNull(type);
        return mapper.readValue(value, type);
    }
}
