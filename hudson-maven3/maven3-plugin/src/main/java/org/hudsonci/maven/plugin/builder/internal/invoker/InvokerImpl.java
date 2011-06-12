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

package org.hudsonci.maven.plugin.builder.internal.invoker;

import org.sonatype.gossip.support.DC;
import org.hudsonci.maven.eventspy.common.Callback;
import hudson.remoting.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hudsonci.utils.common.Varargs.$;

/**
 * Default {@link Invoker}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class InvokerImpl
    implements Invoker, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(InvokerImpl.class);

    private final Object target;

    private final Class targetType;

    private final AtomicInteger counter = new AtomicInteger(0);

    private static final Map<MethodKey,Method> methodLookup = new HashMap<MethodKey,Method>();

    static {
        // Prime the method lookup
        log.trace("Method lookup:");
        for (Method method : Callback.class.getDeclaredMethods()) {
            MethodKey key = new MethodKey(method);
            methodLookup.put(key, method);
            log.trace("  {} -> {}", key, method);
        }
    }

    public InvokerImpl(final Object target) {
        assert target != null;
        this.target = target;
        this.targetType = target.getClass();
    }

    public MarshalledObject invoke(final MethodKey key, final MarshalledObject[] margs) throws Throwable {
        assert key != null;
        // margs may be null

        DC.put(InvokerImpl.class, counter.getAndIncrement());
        boolean trace = log.isTraceEnabled();

        try {
            Object[] args = null;

            // If there are arguments, then decode them
            if (margs != null) {
                args = new Object[margs.length];
                for (int i=0; i<args.length; i++) {
                    args[i] = margs[i].get();
                }
            }

            // Pick the target method and invoke it
            Method method = methodLookup.get(key);
            if (method == null) {
                log.error("Missing method for key: {}", key);
                throw new Error(String.format("Missing method for key: %s", key));
            }

            log.trace("Selected: {}", method);

            Object result;
            try {
                result = method.invoke(target, args);
            }
            catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                log.error("Invocation threw exception", cause);
                // TODO: Eh, probably in this case want to marshall the exception and pass it back to the caller?
                // TODO: ... not sure how this is normally done in RMI-land
                throw cause;
            }
            catch (Throwable t) {
                log.error("Invoke failed", t);
                throw t;
            }

            if (trace) {
                log.trace("Result:");
                if (result != null) {
                    Class vtype = result.getClass();
                    log.trace("  {} ({}@{})", $(result, vtype, vtype.hashCode()));
                }
                else {
                    log.trace("  null");
                }
            }

            // If there is a non-void result then encode it
            if (result != null && result.getClass() != Void.TYPE) {
                //noinspection unchecked
                return new MarshalledObject(result);
            }

            return null;
        }
        finally {
            DC.remove(InvokerImpl.class);
        }
    }

    /**
     * Converts to a proxy for use on remote node.
     */
    private Object writeReplace() {
        return Channel.current().export(Invoker.class, this);
    }
}
