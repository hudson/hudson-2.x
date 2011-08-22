/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.XppReader;
import hudson.model.Descriptor;
import hudson.util.AtomicFileWriter;
import hudson.util.IOException2;
import hudson.util.XStream2;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Represents an XML data file that Hudson uses as a data file.
 *
 *
 * <h2>Evolving data format</h2>
 * <p>
 * Changing data format requires a particular care so that users with
 * the old data format can migrate to the newer data format smoothly.
 *
 * <p>
 * Adding a field is the easiest. When you read an old XML that does
 * not have any data, the newly added field is left to the VM-default
 * value (if you let XStream create the object, such as
 * {@link #read()} &mdash; which is the majority), or to the value initialized by the
 * constructor (if the object is created via <tt>new</tt> and then its
 * value filled by XStream, such as {@link #unmarshal(Object)}.)
 *
 * <p>
 * Removing a field requires that you actually leave the field with
 * <tt>transient</tt> keyword. When you read the old XML, XStream
 * will set the value to this field. But when the data is saved,
 * the field will no longer will be written back to XML.
 * (It might be possible to tweak XStream so that we can simply
 * remove fields from the class. Any help appreciated.)
 *
 * <p>
 * Changing the data structure is usually a combination of the two
 * above. You'd leave the old data store with <tt>transient</tt>,
 * and then add the new data. When you are reading the old XML,
 * only the old field will be set. When you are reading the new XML,
 * only the new field will be set. You'll then need to alter the code
 * so that it will be able to correctly handle both situations,
 * and that as soon as you see data in the old field, you'll have to convert
 * that into the new data structure, so that the next <tt>save</tt> operation
 * will write the new data (otherwise you'll end up losing the data, because
 * old fields will be never written back.)
 *
 * <p>
 * In some limited cases (specifically when the class is the root object
 * to be read from XML, such as {@link Descriptor}), it is possible
 * to completely and drastically change the data format. See
 * {@link Descriptor#load()} for more about this technique.
 *
 * <p>
 * There's a few other possibilities, such as implementing a custom
 * {@link Converter} for XStream, or {@link XStream#alias(String, Class) registering an alias}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class XmlFile {
    private final XStream xs;
    private final File file;

    public XmlFile(File file) {
        this(DEFAULT_XSTREAM,file);
    }

    public XmlFile(XStream xs, File file) {
        this.xs = xs;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     * Loads the contents of this file into a new object.
     */
    public Object read() throws IOException {
        LOGGER.fine("Reading "+file);
        Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            return xs.fromXML(r);
        } catch(StreamException e) {
            throw new IOException2("Unable to read "+file,e);
        } catch(ConversionException e) {
            throw new IOException2("Unable to read "+file,e);
        } catch(Error e) {// mostly reflection errors
            throw new IOException2("Unable to read "+file,e);
        } finally {
            r.close();
        }
    }

    /**
     * Loads the contents of this file into an existing object.
     *
     * @return
     *      The unmarshalled object. Usually the same as <tt>o</tt>, but would be different
     *      if the XML representation is completely new.
     */
    public Object unmarshal( Object o ) throws IOException {
        Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        try {
            return xs.unmarshal(new XppReader(r),o);
        } catch (StreamException e) {
            throw new IOException2("Unable to read "+file,e);
        } catch(ConversionException e) {
            throw new IOException2("Unable to read "+file,e);
        } catch(Error e) {// mostly reflection errors
            throw new IOException2("Unable to read "+file,e);
        } finally {
            r.close();
        }
    }

    public void write( Object o ) throws IOException {
        mkdirs();
        AtomicFileWriter w = new AtomicFileWriter(file);
        try {
            w.write("<?xml version='1.0' encoding='UTF-8'?>\n");
            xs.toXML(o,w);
            w.commit();
        } catch(StreamException e) {
            throw new IOException2(e);
        } finally {
            w.abort();
        }
    }

    public boolean exists() {
        return file.exists();
    }

    public void delete() {
        file.delete();
    }
    
    public void mkdirs() {
        file.getParentFile().mkdirs();
    }

    @Override
    public String toString() {
        return file.toString();
    }

    /**
     * Opens a {@link Reader} that loads XML.
     * This method uses {@link #sniffEncoding() the right encoding},
     * not just the system default encoding.
     * @deprecated Should not be loading XML content using a character stream.
     */
    @Deprecated
    public Reader readRaw() throws IOException {
        return new InputStreamReader(new FileInputStream(file),sniffEncoding());
    }

    /**
     * Returns the XML file read as a string.
     * @deprecated Should not be loading XML content using a character stream.
     */
    @Deprecated
    public String asString() throws IOException {
        StringWriter w = new StringWriter();
        writeRawTo(w);
        return w.toString();
    }

    /**
     * Writes the raw XML to the given {@link Writer}.
     * Writer will not be closed by the implementation.
     * @deprecated Safer to use {@link #writeRawTo(OutputStream)}.
     */
    @Deprecated
    public void writeRawTo(Writer w) throws IOException {
        Reader r = readRaw();
        try {
            Util.copyStream(r,w);
        } finally {
            r.close();
        }
    }

    /**
     * Writes the raw XML to the given {@link OutputStream}.
     * Stream will not be closed by the implementation.
     * @since 2.1.1
     */
    public void writeRawTo(OutputStream os) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            Util.copyStream(is, os);
        } finally {
            is.close();
        }
    }

    /**
     * Parses the beginning of the file and determines the encoding.
     *
     * @throws IOException
     *      if failed to detect encoding.
     * @return
     *      always non-null.
     * @deprecated Should not be loading XML content using a character stream.
     */
    @Deprecated
    public String sniffEncoding() throws IOException {
        class Eureka extends SAXException {
            final String encoding;
            public Eureka(String encoding) {
                this.encoding = encoding;
            }
        }
        try {
            JAXP.newSAXParser().parse(file,new DefaultHandler() {
                private Locator loc;
                @Override
                public void setDocumentLocator(Locator locator) {
                    this.loc = locator;
                }

                @Override
                public void startDocument() throws SAXException {
                    attempt();
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    attempt();
                    // if we still haven't found it at the first start element,
                    // there's something wrong.
                    throw new Eureka(null);
                }

                private void attempt() throws Eureka {
                    if(loc==null)   return;
                    if (loc instanceof Locator2) {
                        Locator2 loc2 = (Locator2) loc;
                        String e = loc2.getEncoding();
                        if(e!=null)
                            throw new Eureka(e);
                    }
                }
            });
            // can't reach here
            throw new AssertionError();
        } catch (Eureka e) {
            if(e.encoding==null)
                throw new IOException("Failed to detect encoding of "+file);
            return e.encoding;
        } catch (SAXException e) {
            throw new IOException2("Failed to detect encoding of "+file,e);
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);    // impossible
        }
    }

    /**
     * {@link XStream} instance is supposed to be thread-safe.
     */
    public static final XStream DEFAULT_XSTREAM = new XStream2();

    private static final Logger LOGGER = Logger.getLogger(XmlFile.class.getName());

    private static final SAXParserFactory JAXP = SAXParserFactory.newInstance();

    static {
        JAXP.setNamespaceAware(true);
    }
}
