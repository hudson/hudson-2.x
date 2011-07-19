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

package hudson;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;

/**
 * classLoader which use first /WEB-INF/lib/*.jar and /WEB-INF/classes before core classLoader
 * <b>you must use the pluginFirstClassLoader true in the maven-hpi-plugin</b>
 * @author olamy
 * @since 1.371
 */
public class PluginFirstClassLoader
    extends AntClassLoader
    implements Closeable
{
    
    private List<URL> urls = new ArrayList<URL>();

    public void addPathFiles( Collection<File> paths )
        throws IOException
    {
        for ( File f : paths )
        {
            urls.add( f.toURI().toURL() );
            addPathFile( f );
        }
    }

    /**
     * @return List of jar used by the plugin /WEB-INF/lib/*.jar and classes directory /WEB-INF/classes
     */
    public List<URL> getURLs() 
    {
        return urls;
    }
    
    public void close()
        throws IOException
    {
        cleanup();
    }

    @Override
    protected Enumeration findResources( String arg0, boolean arg1 )
        throws IOException
    {
        Enumeration enu = super.findResources( arg0, arg1 );
        return enu;
    }

    @Override
    protected Enumeration findResources( String name )
        throws IOException
    {
        Enumeration enu = super.findResources( name );
        return enu;
    }

    @Override
    public URL getResource( String arg0 )
    {
        URL url = super.getResource( arg0 );
        return url;
    }

    @Override
    public InputStream getResourceAsStream( String name )
    {
        InputStream is = super.getResourceAsStream( name );
        return is;
    }   
    
}
