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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.maven;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.hudson.legacy.maven.plugin.MojoInfo;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.EmbedderLoggerImpl
 */
public final class ExecutedMojo extends org.eclipse.hudson.legacy.maven.plugin.ExecutedMojo implements Serializable {

	private static final long serialVersionUID = 1L;

	public ExecutedMojo(MojoInfo mojo, long duration) throws IOException,
			InterruptedException {
		super(mojo, duration);
	}
    
}
