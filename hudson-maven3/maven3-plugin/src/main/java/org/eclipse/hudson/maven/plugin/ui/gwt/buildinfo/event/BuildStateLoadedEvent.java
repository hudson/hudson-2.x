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

import com.google.gwt.event.shared.EventHandler;

import org.eclipse.hudson.gwt.common.EventSupport;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent.Handler;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.BuildStatesDTO;

import java.util.List;


/**
 * Indicates that {@link BuildStateDTO}s have been loaded.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class BuildStateLoadedEvent
    extends EventSupport<Handler>
{
    /**
     * Handler interface for {@link BuildStateLoadedEvent} events.
     */
    public static interface Handler
        extends EventHandler
    {
        void onLoad( BuildStateLoadedEvent event );
    }

    public static Type<Handler> TYPE = new Type<Handler>();

    private final BuildStatesDTO states;

    public BuildStateLoadedEvent( final BuildStatesDTO states )
    {
        super(TYPE);
        this.states = states;
    }

    public List<BuildStateDTO> getBuildStates()
    {
        return states.getStates();
    }

    @Override
    public Type<Handler> getAssociatedType()
    {
        return TYPE;
    }

    @Override
    protected void dispatch( Handler handler )
    {
        handler.onLoad( this );
    }
}
