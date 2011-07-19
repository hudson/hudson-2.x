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

package org.eclipse.hudson.rest.client;

import java.util.Collections;
import java.util.List;

/**
 * Thrown to indicate a failure to perform the client-server handshake.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class HandshakeFailedException
    extends HandshakeException
{
    private final List<Throwable> failures;

    public HandshakeFailedException(final String message, final List<Throwable> failures) {
        super(message);
        assert failures != null;
        this.failures = Collections.unmodifiableList(failures);
    }

    public List<Throwable> getFailures() {
        return failures;
    }
}
