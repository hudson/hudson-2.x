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

package hudson.remoting;

import java.io.Serializable;

/**
 * One-way command to be sent over to the remote system and executed there.
 * This is layer 0, the lower most layer.
 * <p/>
 * <p/>
 * At this level, remoting of class files are not provided, so both {@link Channel}s
 * need to have the definition of {@link Command}-implementation.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class Command implements Serializable {
    /**
     * This exception captures the stack trace of where the Command object is created.
     * This is useful for diagnosing the error when command fails to execute on the remote peer.
     */
    public final Exception createdAt;


    protected Command() {
        this(true);
    }

    protected Command(Throwable cause) {
        this.createdAt = new Source(cause);
    }

    /**
     * @param recordCreatedAt If false, skip the recording of where the command is created. This makes the trouble-shooting
     * and cause/effect correlation hard in case of a failure, but it will reduce the amount of the data
     * transferred.
     */
    protected Command(boolean recordCreatedAt) {
        if (recordCreatedAt) {
            this.createdAt = new Source();
        } else {
            this.createdAt = null;
        }
    }

    /**
     * Called on a remote system to perform this command.
     *
     * @param channel The {@link Channel} of the remote system.
     */
    protected abstract void execute(Channel channel);

    private static final long serialVersionUID = 1L;

    private final class Source extends Exception {
        public Source() {
        }

        private Source(Throwable cause) {
            super(cause);
        }

        public String toString() {
            return "Command " + Command.this.toString() + " created at";
        }

        private static final long serialVersionUID = 1L;
    }
}
