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
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegates to another {@link OutputStream}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DelegatingOutputStream
    extends OutputStream
{
    private OutputStream delegate;

    public DelegatingOutputStream(final OutputStream delegate) {
        setDelegate(delegate);
    }

    protected OutputStream getDelegate() {
        return delegate;
    }

    protected void setDelegate(final OutputStream delegate) {
        this.delegate = checkNotNull(delegate);
    }

    public void write(final int b) throws IOException {
        getDelegate().write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        getDelegate().write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        getDelegate().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        getDelegate().flush();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }
}
