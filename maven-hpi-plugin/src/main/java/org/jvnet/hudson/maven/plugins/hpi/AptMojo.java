/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        Kohsuke Kawaguchi
 *
 *******************************************************************************/ 

package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.Field;

/**
 * @goal apt-compile
 * @phase compile
 * @requiresDependencyResolution compile
 * @author Kohsuke Kawaguchi
 */
public class AptMojo extends CompilerMojo {
    public void execute() throws MojoExecutionException, CompilationFailureException {
        // overwrite the compilerId value. This seems to be the only way to
        //do so without touching the copied files.
        setField("compilerId", "hpi-apt");

        if(!isMustangOrAbove())
            throw new MojoExecutionException("JDK6 or later is necessary to build a Hudson plugin");

        super.execute();
    }

    /**
     * Are we running on JDK6 or above?
     */
    private static boolean isMustangOrAbove() {
        try {
            Class.forName("javax.annotation.processing.Processor");
            return true;
        } catch(ClassNotFoundException e) {
            return false;
        }
    }

    private void setField(String name, String value) {
        try {
            Field field = AbstractCompilerMojo.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e); // impossible
        } catch (IllegalAccessException e) {
            throw new AssertionError(e); // impossible
        }
    }
}
