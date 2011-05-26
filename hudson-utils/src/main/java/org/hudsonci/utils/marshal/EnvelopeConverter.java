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

package org.hudsonci.utils.marshal;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converter for {@link Envelope} types.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class EnvelopeConverter
    implements Converter
{
    private static final Logger log = LoggerFactory.getLogger(EnvelopeConverter.class);

    private final Mapper mapper;

    private final ReflectionProvider reflection;

    public EnvelopeConverter(final Mapper mapper, final ReflectionProvider reflection) {
        this.mapper = checkNotNull(mapper);
        this.reflection = checkNotNull(reflection);

    }

    public boolean canConvert(final Class type) {
        return Envelope.class.isAssignableFrom(type);
    }

    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        assert source != null;
        assert writer != null;
        assert context != null;

        // TODO: Pick appropriate handler for serializng based on envelop version

        Envelope envelope = (Envelope)source;

        log.debug("Envelope: {}",  envelope);
        log.debug("  Version: {}",  envelope.getVersion());
        log.debug("  Serial: {}",  envelope.getSerial());

        Object content = envelope.getContent();
        assert content != null;
        Class contentType = content.getClass();

        log.debug("Content: {}",  content);
        log.debug("  Type: {}",  contentType);
        log.debug("  Default Impl: {}",  mapper.defaultImplementationOf(contentType));
        log.debug("  Serialized name: {}",  mapper.serializedClass(contentType));
        log.debug("  Version: {}",  SerialVersionHelper.get(content));

        writer.addAttribute("version", String.valueOf(envelope.getVersion()));
        writer.addAttribute("serial", String.valueOf(envelope.getSerial()));

        writer.startNode("type");
        writer.addAttribute("class", mapper.serializedClass(contentType));
        writer.addAttribute("version", String.valueOf(SerialVersionHelper.get(content)));

        writer.startNode("content");
        context.convertAnother(content);
        writer.endNode();

        writer.endNode();
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        assert reader != null;
        assert context != null;

        int version = Integer.valueOf(reader.getAttribute("version"));
        long serial = Long.valueOf(reader.getAttribute("serial"));

        // TODO: Pick appropriate handler for deserializng based on envelop version

        log.debug("Envelope:");
        log.debug("  Version: {}",  version);
        log.debug("  Serial: {}",  serial);

        reader.moveDown();

        String contentAlias = reader.getAttribute("class");
        Class contentType = mapper.realClass(contentAlias);
        String contentVersion = reader.getAttribute("version");

        log.debug("Content:");
        log.debug("  Alias: {}",  contentAlias);
        log.debug("  Type: {}",  contentType);
        log.debug("  Version: {}",  contentVersion);

        reader.moveDown();

        // TODO: This is the point of version translation

        Object value = context.convertAnother(null, contentType);

        log.debug("  Value: {}",  value);

        return new Envelope(version, serial, value);
    }
}
