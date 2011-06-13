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

package org.hudsonci.rest.common;

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
