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
 *   Olivier Lamy
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenEmbedderUtils;
import hudson.maven.MavenInformation;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;

import org.kohsuke.stapler.framework.io.IOException2;

/**
 * 
 * @author Olivier Lamy
 * @since 3.0
 *
 */
public class MavenVersionCallable
    implements Callable<MavenInformation, IOException>
{
    
    private final String mavenHome;
    
    public MavenVersionCallable( String mavenHome )
    {
        this.mavenHome = mavenHome;
    }

    public MavenInformation call()
        throws IOException
    {
        try
        {
            return MavenEmbedderUtils.getMavenVersion( new File(mavenHome) );
        }
        catch ( MavenEmbedderException e )
        {
            throw new IOException2( e );
        }
    }

}
