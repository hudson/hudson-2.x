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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event;

import org.eclipse.hudson.gwt.common.EventSupport;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.ModuleSelectedEvent.Handler;

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

/**
 * Indicates that a {@link MavenProjectDTO} has been selected.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ModuleSelectedEvent
    extends EventSupport<Handler>
{
    /**
     * Handler interface for {@link ModuleSelectedEvent} events.
     */
    public static interface Handler
        extends EventHandler
    {
        void onModulePicked( ModuleSelectedEvent event );
    }

    public static Type<Handler> TYPE = new Type<Handler>();

    private final MavenProjectDTO module;

    public ModuleSelectedEvent( final MavenProjectDTO selectedModule )
    {
        super(TYPE);
        this.module = selectedModule;
    }

    public MavenProjectDTO getModule()
    {
        return module;
    }

    @Override
    public Type<Handler> getAssociatedType()
    {
        return TYPE;
    }

    @Override
    protected void dispatch( Handler handler )
    {
        handler.onModulePicked( this );
    }
}
