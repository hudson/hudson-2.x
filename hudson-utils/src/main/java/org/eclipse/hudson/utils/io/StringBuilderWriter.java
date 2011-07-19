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

package org.eclipse.hudson.utils.io;

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
