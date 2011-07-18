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

package org.jvnet.hudson.maven3.agent;

import java.io.File;


/**
 * Exists solely for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven3.agent.Maven3Main
 */
public class Maven3Main {
	 
	public static void main(File m2Home, File remotingJar, File interceptorJar,
			int tcpPort) throws Exception {
		org.eclipse.hudson.legacy.maven3.agent.Maven3Main.main(m2Home, remotingJar, interceptorJar, tcpPort);
	}
	
	public static int launch(String[] args) throws Exception {
		return org.eclipse.hudson.legacy.maven3.agent.Maven3Main.launch(args);
	}
	
}
