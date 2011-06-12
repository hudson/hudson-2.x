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
import hudson.util.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hudsonci.utils.common.Varargs.$;

/**
 * Handles de-typed invocation from local to remote via {@link Invoker}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class RemoteInvokeHandler
    implements InvocationHandler
{
    private static final Logger log = LoggerFactory.getLogger(RemoteInvokeHandler.class);

    private final Invoker invoker;

    private final AtomicInteger counter = new AtomicInteger(0);

    private final Map<Method,MethodKey> keyCache = new CopyOnWriteMap.Hash<Method,MethodKey>();

    public RemoteInvokeHandler(final Invoker invoker) {
        assert invoker != null;
        this.invoker = invoker;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert proxy != null;
        assert method != null;
        // args may be null

        DC.put(RemoteInvokeHandler.class, counter.getAndIncrement());
        boolean trace = log.isTraceEnabled();

        // Lots of debugging here, don't be scared
        log.trace("Invoking method: {}", method);

        Class[] ptypes = method.getParameterTypes();
        if (trace) {
            if (ptypes.length != 0) {
                log.trace("Parameters:");
                    for (Class type : ptypes) {
                    log.trace("  {}@{}", type, type.hashCode());
                }
            }

            Class rtype = method.getReturnType();
            log.trace("Returns:");
            if (rtype != Void.TYPE) {
                log.trace("  {}@{}", rtype, rtype.hashCode());
            }
            else {
                log.trace("  void");
            }

            if (args != null) {
                log.trace("Arguments:");
                for (Object arg : args) {
                    if (arg != null) {
                        Class atype = arg.getClass();
                        log.trace("  {} ({}@{})", $(arg, atype, atype.hashCode()));
                    }
                    else {
                        log.trace("  null");
                    }
                }
            }
        }

        // Pick/cache the method key
        MethodKey key = keyCache.get(method);
        if (key == null) {
            key = new MethodKey(method);
            keyCache.put(method, key);
        }

        log.trace("Method key: {}", key);

        MarshalledObject[] margs = null;

        // If there are args then encode them
        if (args != null) {
            margs = new MarshalledObject[args.length];
            for (int i=0; i<args.length; i++) {
                //noinspection unchecked
                margs[i] = new MarshalledObject(args[i]);
            }
        }

        try {
            MarshalledObject mresult;
            try {
                mresult = invoker.invoke(key, margs);
            }
            catch (InvocationTargetException e) {
                log.error("Invoke failed", e.getTargetException());
                throw e;
            }

            // If we have a result, then decode it
            Object result = null;
            if (mresult != null) {
                result = mresult.get();
            }

            if (trace) {
                log.trace("Result:");
                if (result != null) {
                    Class vtype = result.getClass();
                    log.trace("    {} ({}@{})", $(result, vtype, vtype.hashCode()));
                }
                else {
                    log.trace("    null");
                }
            }

            return result;
        }
        finally {
            DC.remove(RemoteInvokeHandler.class);
        }
    }
}
