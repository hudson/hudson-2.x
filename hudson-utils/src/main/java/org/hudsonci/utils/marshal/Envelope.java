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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Envelope for persisting objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0.1
 *
 * @See EnvelopeConverter
 */
@XStreamAlias("envelope")
//@XStreamConverter(EnvelopeConverter.class) // FIXME: See what this does if anything for converter w/ctor args
public class Envelope<T>
{
    public static final int VERSION = 1;

    private final int version;

    private final long serial;

    private final T content;

    //
    // TODO: Add support for attributes, but leave this for a VERSION 2 so we can validate the interop
    //

//    private Map<String,Object> attributes;

    public Envelope(final T content) {
        this(System.nanoTime(), content);
    }

    Envelope(final long serial, final T content) {
        this(VERSION, serial, content);
    }

    Envelope(final int version, final long serial, final T content) {
        this.version = version;
        this.serial = serial;
        this.content = content;
    }

    public int getVersion() {
        return version;
    }

    public long getSerial() {
        return serial;
    }

    public T getContent() {
        return content;
    }

//    public Map<String,Object> getAttributes() {
//        if (attributes == null) {
//            attributes = new HashMap<String,Object>();
//        }
//        return attributes;
//    }

    @Override
    public String toString() {
        return "Envelope{" +
            "version=" + version +
            ", serial=" + serial +
            ", content=" + content +
            '}';
    }
}