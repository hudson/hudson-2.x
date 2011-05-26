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

package org.hudsonci.utils.io;

import java.io.Writer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link Writer} buffer using {@link StringBuilder}.
 *
 * @since 2.1.0
 */
public class StringBuilderWriter
    extends Writer
{
    private final StringBuilder buffer;

    public StringBuilderWriter() {
        this(new StringBuilder());
    }

    private StringBuilderWriter(final StringBuilder buffer) {
        this.buffer = checkNotNull(buffer);
        this.lock = buffer;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    @Override
    public void write(final int c) {
        buffer.append((char) c);
    }

    @Override
    public void write(final char cbuf[], final int off, final int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        else if (len == 0) {
            return;
        }
        buffer.append(cbuf, off, len);
    }

    @Override
    public void write(final String str) {
        buffer.append(str);
    }

    @Override
    public void write(final String str, final int off, final int len) {
        buffer.append(str.substring(off, off + len));
    }

    @Override
    public StringBuilderWriter append(final CharSequence csq) {
        if (csq == null) {
            write("null");
        }
        else {
            write(csq.toString());
        }
        return this;
    }

    @Override
    public StringBuilderWriter append(final CharSequence csq, final int start, final int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    @Override
    public StringBuilderWriter append(final char c) {
        write(c);
        return this;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    @Override
    public void flush() {
        // nop
    }

    @Override
    public void close() {
        // nop
    }
}
