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
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.event.BuildStateSelectedEvent.Handler;

/**
 * Indicates that a {@link BuildStateDTO} has been selected.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class BuildStateSelectedEvent
    extends EventSupport<Handler>
{
    /**
     * Handler interface for {@link BuildStateSelectedEvent} events.
     */
    public static interface Handler
        extends EventHandler
    {
        void onSelected( BuildStateSelectedEvent event );
    }

    public static Type<Handler> TYPE = new Type<Handler>();

    private final BuildStateDTO state;

    public BuildStateSelectedEvent( final BuildStateDTO selectedState )
    {
        super(TYPE);
        this.state = selectedState;
    }

    public BuildStateDTO getBuildState()
    {
        return state;
    }

    @Override
    public Type<Handler> getAssociatedType()
    {
        return TYPE;
    }

    @Override
    protected void dispatch( Handler handler )
    {
        handler.onSelected( this );
    }
}
