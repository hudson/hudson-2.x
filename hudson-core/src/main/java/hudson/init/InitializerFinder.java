/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.init;

import org.jvnet.hudson.annotation_indexer.Index;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Task;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.MilestoneImpl;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.localizer.ResourceBundleHolder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.model.Hudson;

import static java.util.logging.Level.FINEST;

/**
 * Discovers initialization tasks from {@link Initializer}.
 *
 * @author Kohsuke Kawaguchi
 */
public class InitializerFinder extends TaskBuilder {
    private final ClassLoader cl;

    private final Set<Method> discovered = new HashSet<Method>();

    public InitializerFinder(ClassLoader cl) {
        this.cl = cl;
    }

    public InitializerFinder() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public Collection<Task> discoverTasks(Reactor session) throws IOException {
        List<Task> result = new ArrayList<Task>();
        for (Method e : Index.list(Initializer.class,cl,Method.class)) {
            if (!discovered.add(e))
                continue;   // already reported once

            if (!Modifier.isStatic(e.getModifiers()))
                throw new IOException(e+" is not a static method");

            Initializer i = e.getAnnotation(Initializer.class);
            if (i==null)        continue; // stale index

            result.add(new TaskImpl(i, e));
        }
        return result;
    }

    /**
     * Obtains the display name of the given initialization task
     */
    protected String getDisplayNameOf(Method e, Initializer i) {
        Class<?> c = e.getDeclaringClass();
        try {
            ResourceBundleHolder rb = ResourceBundleHolder.get(c.getClassLoader().loadClass(c.getPackage().getName() + ".Messages"));

            String key = i.displayName();
            if (key.length()==0)  return c.getSimpleName()+"."+e.getName();
            return rb.format(key);
        } catch (ClassNotFoundException x) {
            LOGGER.log(FINEST, "Failed to load "+x.getMessage()+" for "+e.toString(),x);
            return c.getSimpleName()+"."+e.getName();
        }
    }

    /**
     * Invokes the given initialization method.
     */
    protected void invoke(Method e) {
        try {
            Class<?>[] pt = e.getParameterTypes();
            Object[] args = new Object[pt.length];
            for (int i=0; i<args.length; i++)
                args[i] = lookUp(pt[i]);
            e.invoke(null,args);
        } catch (IllegalAccessException x) {
            throw (Error)new IllegalAccessError().initCause(x);
        } catch (InvocationTargetException x) {
            throw new Error(x);
        }
    }

    /**
     * Determines the parameter injection of the initialization method.
     */
    private Object lookUp(Class<?> type) {
        if (type== Hudson.class)
            return Hudson.getInstance();
        throw new IllegalArgumentException("Unable to inject "+type);
    }

    /**
     * Task implementation.
     */
    public class TaskImpl implements Task {
        final Collection<Milestone> requires;
        final Collection<Milestone> attains;
        private final Initializer i;
        private final Method e;

        private TaskImpl(Initializer i, Method e) {
            this.i = i;
            this.e = e;
            requires = toMilestones(i.requires(), i.after());
            attains = toMilestones(i.attains(), i.before());
        }

        /**
         * {@link Initializer} annotaion on the {@linkplain #getMethod() method}
         */
        public Initializer getAnnotation() {
            return i;
        }

        /**
         * Static method that runs the initialization, that this task wraps.
         */
        public Method getMethod() {
            return e;
        }

        public Collection<Milestone> requires() {
            return requires;
        }

        public Collection<Milestone> attains() {
            return attains;
        }

        public String getDisplayName() {
            return getDisplayNameOf(e, i);
        }

        public boolean failureIsFatal() {
            return i.fatal();
        }

        public void run(Reactor session) {
            invoke(e);
        }

        public String toString() {
            return e.toString();
        }

        private Collection<Milestone> toMilestones(String[] tokens, InitMilestone m) {
            List<Milestone> r = new ArrayList<Milestone>();
            for (String s : tokens) {
                try {
                    r.add(InitMilestone.valueOf(s));
                } catch (IllegalArgumentException x) {
                    r.add(new MilestoneImpl(s));
                }
            }
            r.add(m);
            return r;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(InitializerFinder.class.getName());
}
