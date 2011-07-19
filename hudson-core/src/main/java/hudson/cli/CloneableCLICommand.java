/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.cli;

/**
 * {@link Cloneable} {@link CLICommand}.
 *
 * Uses {@link #clone()} instead of "new" to create a copy for exection.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class CloneableCLICommand extends CLICommand implements Cloneable {
    @Override
    protected CLICommand createClone() {
        try {
            return (CLICommand)clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
