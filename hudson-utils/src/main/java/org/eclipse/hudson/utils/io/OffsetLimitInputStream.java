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
