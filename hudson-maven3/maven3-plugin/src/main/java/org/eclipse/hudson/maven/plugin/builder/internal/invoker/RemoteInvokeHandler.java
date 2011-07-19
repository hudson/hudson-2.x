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

package org.eclipse.hudson.maven.plugin.builder.internal.invoker;

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

import static org.eclipse.hudson.utils.common.Varargs.$;

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
