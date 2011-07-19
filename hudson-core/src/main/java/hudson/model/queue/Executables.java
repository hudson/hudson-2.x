/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model.queue;

import hudson.model.Queue.Executable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Convenience methods around {@link Executable}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Executables {
    /**
     * Due to the return type change in {@link Executable}, the caller needs a special precaution now.
     */
    public static SubTask getParentOf(Executable e) {
        try {
            return _getParentOf(e);
        } catch (AbstractMethodError _) {
            try {
                Method m = e.getClass().getMethod("getParent");
                m.setAccessible(true);
                return (SubTask) m.invoke(e);
            } catch (IllegalAccessException x) {
                throw (Error)new IllegalAccessError().initCause(x);
            } catch (NoSuchMethodException x) {
                throw (Error)new NoSuchMethodError().initCause(x);
            } catch (InvocationTargetException x) {
                Throwable y = x.getTargetException();
                if (y instanceof Error)     throw (Error)y;
                if (y instanceof RuntimeException)     throw (RuntimeException)y;
                throw new Error(x);
            }
        }
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    private static SubTask _getParentOf(Executable e) {
        return e.getParent();
    }

    /**
     * Returns the estimated duration for the executable.
     * Protects against {@link AbstractMethodError}s if the {@link Executable} implementation
     * was compiled against Hudson < 1.383
     */
    public static long getEstimatedDurationFor(Executable e) {
        try {
            return _getEstimatedDuration(e);
        } catch (AbstractMethodError error) {
            return e.getParent().getEstimatedDuration();
        }
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    private static long _getEstimatedDuration(Executable e) {
        return e.getEstimatedDuration();
    }
}
