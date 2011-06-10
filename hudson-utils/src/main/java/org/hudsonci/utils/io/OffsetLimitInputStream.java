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

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides read-offset and read-limit for an underlying input-stream.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class OffsetLimitInputStream
    extends InputStream
{
    public static final int UNLIMITED = -1;

    private final InputStream delegate;

    private final long offset;

    private final long length;

    private long count = 0;

    public OffsetLimitInputStream(final InputStream delegate, final long offset, final long length) throws IOException {
        this.delegate = checkNotNull(delegate);
        this.offset = offset;
        this.length = length;

        delegate.skip(offset);
    }

    public InputStream getDelegate() {
        return delegate;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    public long getCount() {
        return count;
    }

    @Override
    public int read() throws IOException {
        if (length > UNLIMITED && count > length) {
            return -1;
        }

        count++;

        return getDelegate().read();
    }

    @Override
    public int available() throws IOException {
        return getDelegate().available();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        getDelegate().mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        getDelegate().reset();
    }

    @Override
    public boolean markSupported() {
        return getDelegate().markSupported();
    }
}
