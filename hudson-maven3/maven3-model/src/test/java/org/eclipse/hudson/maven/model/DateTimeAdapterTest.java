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

import org.eclipse.hudson.maven.model.DateTimeAdapter;
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
