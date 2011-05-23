/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.event;

import com.google.gwt.event.shared.EventHandler;
import com.sonatype.matrix.gwt.common.EventSupport;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.BuildStatesDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent.Handler;

import java.util.List;

/**
 * Indicates that {@link BuildStateDTO}s have been loaded.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
