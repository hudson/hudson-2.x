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

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegating {@link com.thoughtworks.xstream.XStream}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0.1
 */
public class DelegatingXStream
    extends XStream
{
    private final XStream delegate;

    public DelegatingXStream(final XStream delegate) {
        this.delegate = checkNotNull(delegate);
    }

    public XStream getDelegate() {
        return delegate;
    }

    public void setMarshallingStrategy(MarshallingStrategy marshallingStrategy) {
        getDelegate().setMarshallingStrategy(marshallingStrategy);
    }

    public String toXML(Object obj) {
        return getDelegate().toXML(obj);
    }

    public void toXML(Object obj, Writer out) {
        getDelegate().toXML(obj, out);
    }

    public void toXML(Object obj, OutputStream out) {
        getDelegate().toXML(obj, out);
    }

    public void marshal(Object obj, HierarchicalStreamWriter writer) {
        getDelegate().marshal(obj, writer);
    }

    public void marshal(Object obj, HierarchicalStreamWriter writer, DataHolder dataHolder) {
        getDelegate().marshal(obj, writer, dataHolder);
    }

    public Object fromXML(String xml) {
        return getDelegate().fromXML(xml);
    }

    public Object fromXML(Reader xml) {
        return getDelegate().fromXML(xml);
    }

    public Object fromXML(InputStream input) {
        return getDelegate().fromXML(input);
    }

    public Object fromXML(String xml, Object root) {
        return getDelegate().fromXML(xml, root);
    }

    public Object fromXML(Reader xml, Object root) {
        return getDelegate().fromXML(xml, root);
    }

    public Object fromXML(InputStream xml, Object root) {
        return getDelegate().fromXML(xml, root);
    }

    public Object unmarshal(HierarchicalStreamReader reader) {
        return getDelegate().unmarshal(reader);
    }

    public Object unmarshal(HierarchicalStreamReader reader, Object root) {
        return getDelegate().unmarshal(reader, root);
    }

    public Object unmarshal(HierarchicalStreamReader reader, Object root, DataHolder dataHolder) {
        return getDelegate().unmarshal(reader, root, dataHolder);
    }

    public void alias(String name, Class type) {
        getDelegate().alias(name, type);
    }

    public void aliasType(String name, Class type) {
        getDelegate().aliasType(name, type);
    }

    public void alias(String name, Class type, Class defaultImplementation) {
        getDelegate().alias(name, type, defaultImplementation);
    }

    public void aliasPackage(String name, String pkgName) {
        getDelegate().aliasPackage(name, pkgName);
    }

    public void aliasField(String alias, Class definedIn, String fieldName) {
        getDelegate().aliasField(alias, definedIn, fieldName);
    }

    public void aliasAttribute(String alias, String attributeName) {
        getDelegate().aliasAttribute(alias, attributeName);
    }

    public void aliasSystemAttribute(String alias, String systemAttributeName) {
        getDelegate().aliasSystemAttribute(alias, systemAttributeName);
    }

    public void aliasAttribute(Class definedIn, String attributeName, String alias) {
        getDelegate().aliasAttribute(definedIn, attributeName, alias);
    }

    public void useAttributeFor(String fieldName, Class type) {
        getDelegate().useAttributeFor(fieldName, type);
    }

    public void useAttributeFor(Class definedIn, String fieldName) {
        getDelegate().useAttributeFor(definedIn, fieldName);
    }

    public void useAttributeFor(Class type) {
        getDelegate().useAttributeFor(type);
    }

    public void addDefaultImplementation(Class defaultImplementation, Class ofType) {
        getDelegate().addDefaultImplementation(defaultImplementation, ofType);
    }

    public void addImmutableType(Class type) {
        getDelegate().addImmutableType(type);
    }

    public void registerConverter(Converter converter) {
        getDelegate().registerConverter(converter);
    }

    public void registerConverter(Converter converter, int priority) {
        getDelegate().registerConverter(converter, priority);
    }

    public void registerConverter(SingleValueConverter converter) {
        getDelegate().registerConverter(converter);
    }

    public void registerConverter(SingleValueConverter converter, int priority) {
        getDelegate().registerConverter(converter, priority);
    }

    public void registerLocalConverter(Class definedIn, String fieldName, Converter converter) {
        getDelegate().registerLocalConverter(definedIn, fieldName, converter);
    }

    public void registerLocalConverter(Class definedIn, String fieldName, SingleValueConverter converter) {
        getDelegate().registerLocalConverter(definedIn, fieldName, converter);
    }

    public ClassMapper getClassMapper() {
        return getDelegate().getClassMapper();
    }

    public Mapper getMapper() {
        return getDelegate().getMapper();
    }

    public ReflectionProvider getReflectionProvider() {
        return getDelegate().getReflectionProvider();
    }

    public ConverterLookup getConverterLookup() {
        return getDelegate().getConverterLookup();
    }

    public void setMode(int mode) {
        getDelegate().setMode(mode);
    }

    public void addImplicitCollection(Class ownerType, String fieldName) {
        getDelegate().addImplicitCollection(ownerType, fieldName);
    }

    public void addImplicitCollection(Class ownerType, String fieldName, Class itemType) {
        getDelegate().addImplicitCollection(ownerType, fieldName, itemType);
    }

    public void addImplicitCollection(Class ownerType, String fieldName, String itemFieldName, Class itemType) {
        getDelegate().addImplicitCollection(ownerType, fieldName, itemFieldName, itemType);
    }

    public DataHolder newDataHolder() {
        return getDelegate().newDataHolder();
    }

    public ObjectOutputStream createObjectOutputStream(Writer writer) throws IOException {
        return getDelegate().createObjectOutputStream(writer);
    }

    public ObjectOutputStream createObjectOutputStream(HierarchicalStreamWriter writer) throws IOException {
        return getDelegate().createObjectOutputStream(writer);
    }

    public ObjectOutputStream createObjectOutputStream(Writer writer, String rootNodeName) throws IOException {
        return getDelegate().createObjectOutputStream(writer, rootNodeName);
    }

    public ObjectOutputStream createObjectOutputStream(OutputStream out) throws IOException {
        return getDelegate().createObjectOutputStream(out);
    }

    public ObjectOutputStream createObjectOutputStream(OutputStream out, String rootNodeName) throws IOException {
        return getDelegate().createObjectOutputStream(out, rootNodeName);
    }

    public ObjectOutputStream createObjectOutputStream(HierarchicalStreamWriter writer, String rootNodeName) throws IOException {
        return getDelegate().createObjectOutputStream(writer, rootNodeName);
    }

    public ObjectInputStream createObjectInputStream(Reader xmlReader) throws IOException {
        return getDelegate().createObjectInputStream(xmlReader);
    }

    public ObjectInputStream createObjectInputStream(InputStream in) throws IOException {
        return getDelegate().createObjectInputStream(in);
    }

    public ObjectInputStream createObjectInputStream(HierarchicalStreamReader reader) throws IOException {
        return getDelegate().createObjectInputStream(reader);
    }

    public void setClassLoader(ClassLoader classLoader) {
        getDelegate().setClassLoader(classLoader);
    }

    public ClassLoader getClassLoader() {
        return getDelegate().getClassLoader();
    }

    public void omitField(Class definedIn, String fieldName) {
        getDelegate().omitField(definedIn, fieldName);
    }

    public void processAnnotations(Class[] types) {
        getDelegate().processAnnotations(types);
    }

    public void processAnnotations(Class type) {
        getDelegate().processAnnotations(type);
    }

    public void autodetectAnnotations(boolean mode) {
        getDelegate().autodetectAnnotations(mode);
    }
}