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

package org.eclipse.hudson.utils.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for event processing.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class EventProcessorSupport<ContextType, HandlerType extends EventHandler>
    implements EventProcessor<ContextType>
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * All known event handlers.
     */
    private List<HandlerType> handlers;

    /**
     * Cache of event type to selected handler.
     */
    private final Map<Class,HandlerType> eventTypeHandlerMap = new HashMap<Class,HandlerType>();

    /**
     * The default handle to be used, when none accept.
     */
    private HandlerType defaultHandler;

    public EventProcessorSupport(final List<HandlerType> handlers, final HandlerType defaultHandler) {
        this.handlers = checkNotNull( handlers );
        // defaultHandler can be null
        this.defaultHandler = defaultHandler;
    }

    /**
     * Get installed handlers.
     */
    public List<HandlerType> getHandlers() {
        return handlers;
    }

    /**
     * Set installed handlers.
     */
    public void setHandlers(final List<HandlerType> handlers) {
        this.handlers = checkNotNull( handlers );
    }

    /**
     * Get the default handler.
     */
    public HandlerType getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * Set the default handler.
     */
    public void setDefaultHandler(final HandlerType defaultHandler) {
        this.defaultHandler = checkNotNull( defaultHandler );
    }

    /**
     * Initialize event handlers and their event type mapping.
     */
    @SuppressWarnings({"unchecked"})
    public void init(final ContextType context) {
        log.debug("Initializing w/context: {}", context);

        log.debug("Handlers:");
        for (HandlerType handler : getHandlers()) {
            log.debug("  {}", handler);
            handler.init(context);
        }
    }

    /**
     * Process an event.  Selects the event handler and delegates.
     */
    @SuppressWarnings({"unchecked"})
    public void process(final Object event) throws Exception {
        checkNotNull( event );

        log.trace("Processing event: {}", event);

        try {
            // Pick the handler
            HandlerType handler = getEventHandler(event);

            if (handler != null) {
                // Delegate handling of the event
                handler.handle(event);
            }
            else {
                log.warn("Unhandled event: {} ({})", event, event.getClass().getName());
            }
        }
        catch (Exception e) {
            onFailure(e);
        }
    }

    protected void onFailure(final Exception cause) throws Exception {
        log.error("Processing failure", cause);
        throw cause;
    }

    /**
     * Get the event handler for the given event.  If no handlers can accept use default.
     *
     * @return A handler, never null.
     */
    protected HandlerType getEventHandler(final Object event) {
        checkNotNull( event );

        // Check if we have a cached handler
        Class type = event.getClass();
        HandlerType target = eventTypeHandlerMap.get(type);

        if (target == null) {
            for (HandlerType handler : getHandlers()) {
                if (handler.accept(event)) {
                    target = handler;
                    break;
                }
            }

            if (target == null) {
                target = getDefaultHandler();
            }

            if (log.isTraceEnabled()) {
                log.trace("Selected handler: {}, for event: {}", target, type.getName());
            }

            // Cache the result
            eventTypeHandlerMap.put(type, target);
        }

        return target;
    }
}
