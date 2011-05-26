/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.model;

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
