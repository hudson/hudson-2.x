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
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.event.ModuleSelectedEvent.Handler;

/**
 * Indicates that a {@link MavenProjectDTO} has been selected.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
