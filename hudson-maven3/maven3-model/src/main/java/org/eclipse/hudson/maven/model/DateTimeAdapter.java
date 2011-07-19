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

package org.eclipse.hudson.maven.model;

import org.codehaus.jackson.map.util.StdDateFormat;

import java.text.ParseException;
import java.util.Date;

/**
 * Utility to serialize Date objects as ISO 8601 string format.
 *
 * Note: apparently a format with and without the hour:minute separator
 * are valid ISO 8601 dates.  Jackson only handles the version without
 * the separator and as UTC whereas Calendar produces it with the separator
 * and time zone.
 * 
 * Jackson: 2011-02-03T19:25:53.656+0000
 * Calendar: 2011-02-03T14:25:38.649-05:00
 * 
 * Prefer the Jackson version to make the XML consistent with the JSON representation.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class DateTimeAdapter
{
    public static Date parseDate( final String dateString )
    {
        assert dateString != null;
        try
        {
            return StdDateFormat.instance.parse( dateString );
        }
        catch ( ParseException e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    public static String printDate( final Date date )
    {
        assert date != null;
        return StdDateFormat.instance.format( date );
    }
}
