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

import org.hudsonci.maven.model.DateTimeAdapter;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Verifies {@link DateTimeAdapter} parsing.
 * 
 * @author Jamie Whitehouse
 */
public class DateTimeAdapterTest
{
    @Test
    public void parseIso8601JacksonString()
    {
        String Iso8601StringFromJackson = "2011-02-03T19:25:53.656+0000";
        @SuppressWarnings( "unused" )
        String StringFromCalendar = "2011-02-03T14:25:38.649-05:00";

        DateTimeAdapter.parseDate( Iso8601StringFromJackson );
    }
    
    /**
     * Expect that the parsing test verifies accepted string formats that
     * the generator will produce.  If that passes then checking that output
     * formats can be parsed as input is acceptable verification of generator.
     */
    @Test
    public void generatedFormatIsParsable()
    {
        Date originalDate = new Date();
        String dateAsString = DateTimeAdapter.printDate( originalDate );
        Date parsedDate = DateTimeAdapter.parseDate( dateAsString );
        
        assertThat( parsedDate, equalTo( originalDate ) );
    }
}
