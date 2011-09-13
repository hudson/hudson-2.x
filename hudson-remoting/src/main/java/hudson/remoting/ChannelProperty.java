/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

/**
 * A convenient key type for {@link Channel#getProperty(Object)} and {@link Channel#setProperty(Object, Object)}
 *
 * @author Kohsuke Kawaguchi
 */
public class ChannelProperty<T> {
    public final Class<T> type;
    public final String displayName;

    public ChannelProperty(Class<T> type, String displayName) {
        this.type = type;
        this.displayName = displayName;
    }
}
