/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.event;

import com.google.gwt.event.shared.EventHandler;
import org.hudsonci.gwt.common.EventSupport;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.BuildStatesDTO;

import java.util.List;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent.Handler;

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
