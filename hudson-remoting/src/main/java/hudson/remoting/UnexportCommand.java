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

/**
 * {@link Command} that unexports an object.
 * @author Kohsuke Kawaguchi
 */
public class UnexportCommand extends Command {
    private final int oid;

    public UnexportCommand(int oid) {
        this.oid = oid;
    }

    protected void execute(Channel channel) {
        channel.unexport(oid);
    }

    private static final long serialVersionUID = 1L;
}
