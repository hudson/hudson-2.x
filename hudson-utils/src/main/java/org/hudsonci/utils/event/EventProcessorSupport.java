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

package org.hudsonci.utils.event;

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
