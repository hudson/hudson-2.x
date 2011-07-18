/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.maven.agent;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.classworlds.NoSuchRealmException;

/**
 * Exists solely for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.agent.Main
 */ 
public class Main {
    
    public static void main(String[] args) throws Exception {
    	org.eclipse.hudson.legacy.maven.agent.Main.main(args);
    }
 
    public static void main(File m2Home, File remotingJar, File interceptorJar, int tcpPort, File interceptorOverrideJar) throws Exception {
    	org.eclipse.hudson.legacy.maven.agent.Main.main(m2Home, remotingJar, interceptorJar, tcpPort, interceptorOverrideJar);
    }

    
 
    public static int launch(String[] args) throws NoSuchMethodException, IllegalAccessException, NoSuchRealmException, InvocationTargetException, ClassNotFoundException {
    	return org.eclipse.hudson.legacy.maven.agent.Main.launch(args);
    }
}
