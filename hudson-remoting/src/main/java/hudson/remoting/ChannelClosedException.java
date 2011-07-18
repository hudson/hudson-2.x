/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.remoting;

import java.io.IOException;

/**
 * Indicates that the channel is already closed.
 *
 * @author Kohsuke Kawaguchi
 */
public class ChannelClosedException extends IOException {
    /**
     * @deprecated
     *      Use {@link #ChannelClosedException(Throwable)}.
     */
    public ChannelClosedException() {
        super("channel is already closed");
    }

    public ChannelClosedException(Throwable cause) {
        super("channel is already closed");
        initCause(cause);
    }
}
