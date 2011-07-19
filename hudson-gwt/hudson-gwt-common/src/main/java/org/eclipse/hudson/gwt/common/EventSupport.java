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

package org.eclipse.hudson.gwt.common;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Support for GWT event implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class EventSupport<H extends EventHandler>
    extends GwtEvent<H>
{
    private final Type<H> type;

    protected EventSupport(final Type<H> type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public Type<H> getAssociatedType() {
        return type;
    }

    /**
     * Annoyingly GwtEvent's default toString() is highly useless, so return the name of the event class instead.
     */
    @Override
    public String toString() {
        return getClass().getName();
    }
}
