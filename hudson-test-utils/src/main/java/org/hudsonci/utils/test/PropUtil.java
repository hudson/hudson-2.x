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

package org.hudsonci.utils.test;

public class PropUtil
{
    public static String get( String name, String def )
    {
        String val = System.getProperty( name, def );
        if ( val == null || val.length() == 0 || ( val.startsWith( "${" ) && val.endsWith( "}" ) ) )
        {
            val = def;
        }

        return val;
    }

    public static int get( String name, int def )
    {
        return Integer.parseInt( get( name, String.valueOf( def ) ) );
    }
}
