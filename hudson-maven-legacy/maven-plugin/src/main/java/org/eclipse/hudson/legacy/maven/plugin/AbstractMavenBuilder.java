/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Olivier Lamy
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.remoting.DelegatingCallable;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * @author Olivier Lamy
 *
 */
public abstract class AbstractMavenBuilder implements DelegatingCallable<Result,IOException> {
    
    /**
     * Goals to be executed in this Maven execution.
     */
    protected final List<String> goals;
    /**
     * Hudson-defined system properties. These will be made available to Maven,
     * and accessible as if they are specified as -Dkey=value
     */
    protected final Map<String,String> systemProps;
    /**
     * Where error messages and so on are sent.
     */
    protected final BuildListener listener;
    
    protected AbstractMavenBuilder(BuildListener listener, List<String> goals, Map<String, String> systemProps) {
        this.listener = listener;
        this.goals = goals;
        this.systemProps = systemProps;
    }
    
    protected String formatArgs(List<String> args) {
        StringBuilder buf = new StringBuilder("Executing Maven: ");
        for (String arg : args) {
            final String argPassword = "-Dpassword=" ;
            String filteredArg = arg ;
            // check if current arg is password arg. Then replace password by ***** 
            if (arg.startsWith(argPassword)) {
                filteredArg=argPassword+"*********";
            }
            buf.append(' ').append(filteredArg);
        }
        return buf.toString();
    }    
    
    


    protected String format(NumberFormat n, long nanoTime) {
        return n.format(nanoTime/1000000);
    }

    // since reporters might be from plugins, use the uberjar to resolve them.
    public ClassLoader getClassLoader() {
        return Hudson.getInstance().getPluginManager().uberClassLoader;
    }    
    
}
