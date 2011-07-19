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

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.AUTO_DETECT_SETTERS;
import static org.codehaus.jackson.map.SerializationConfig.Feature.AUTO_DETECT_GETTERS;
import static org.codehaus.jackson.map.SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS;
import static org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * <a href="http://jackson.codehaus.org">Jackson</a> {@link ObjectMapper} provider.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ObjectMapperProvider
    implements Provider<ObjectMapper>
{
    private final ObjectMapper mapper;

    public ObjectMapperProvider() {
        final ObjectMapper mapper = new ObjectMapper();

        // Configure Jackson annotations only, JAXB annotations can confuse and produce improper content
        DeserializationConfig dconfig = mapper.getDeserializationConfig();
        dconfig.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        SerializationConfig sconfig = mapper.getSerializationConfig();
        sconfig.setAnnotationIntrospector(new JacksonAnnotationIntrospector());

        // Do not include null values
        sconfig.setSerializationInclusion(NON_NULL);

        // Write dates as ISO-8601
        mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);

        // Disable detection of getters for serialization
        mapper.configure(AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);

        // Disable detection of setters for de-serialization
        mapper.configure(AUTO_DETECT_SETTERS, false);

        this.mapper = mapper;
    }

    public ObjectMapper get() {
        return mapper;
    }
}
