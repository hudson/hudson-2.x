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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link EventHandler} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class EventHandlerSupport<ContextType, EventType>
    implements EventHandler<ContextType, EventType>
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ContextType context;

    private Class eventType;

    public void init(final ContextType context) {
        this.context = checkNotNull( context );
    }

    protected ContextType getContext() {
        return context;
    }

    /**
     * Accepts the event if it is a compatible type; determined by {@link #getEventType}.
     */
    public boolean accept(final Object event) {
        checkNotNull( event );
        return getEventType().isAssignableFrom(event.getClass());
    }

    /**
     * Determines the event type of the handler.
     */
    protected Class getEventType() {
        if (eventType == null) {
            // find public void handle(Object) method
            for (Method method : getClass().getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType() == Void.TYPE &&
                    method.getName().equals("handle") &&
                    method.getParameterTypes().length == 1 &&
                    // Ignore methods with Object as param, generics muck puts this in here and matches when we want the specific typed method
                    method.getParameterTypes()[0] != Object.class)
                {
                    eventType = method.getParameterTypes()[0];
                    log.debug("Determined type: {}, from method: {}", eventType.getName(), method);
                    break;
                }
            }

            if (eventType == null) {
                throw new Error(getClass().getName() + " missing 'public void handle(<?>)' method");
            }
        }

        return eventType;
    }
}
