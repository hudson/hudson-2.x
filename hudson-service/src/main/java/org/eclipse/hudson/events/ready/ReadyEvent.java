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

package org.eclipse.hudson.events.ready;

import hudson.model.Hudson;

import java.util.EventObject;

/**
 * Event fired once the system is ready to be used.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ReadyEvent
    extends EventObject
{
    public ReadyEvent(final Hudson hudson) {
        super(hudson);
    }

    public Hudson getHudson() {
        return (Hudson)getSource();
    }
}
