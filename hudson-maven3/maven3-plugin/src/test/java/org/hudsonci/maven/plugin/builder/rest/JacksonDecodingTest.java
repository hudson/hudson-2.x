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

package org.hudsonci.maven.plugin.builder.rest;

import org.hudsonci.maven.model.state.ExecutionActivityDTO;
import org.hudsonci.maven.model.state.ExecutionActivityTypeDTO;
import org.hudsonci.rest.common.JacksonCodec;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class JacksonDecodingTest
{
    private static final Logger log = LoggerFactory.getLogger( JacksonDecodingTest.class );
    
    @Test
    public void datestampsAreDecoded() throws IOException
    {
        JacksonCodec codec = new JacksonCodec();
        
        Date expectedDate = new Date();
        ExecutionActivityDTO originalDto = new ExecutionActivityDTO().withType( ExecutionActivityTypeDTO.FINISHED ).withTimestamp( expectedDate );
        
        String json = codec.encode( originalDto );
        log.debug( "Encoded DTO as json {}", json );
        
        ExecutionActivityDTO wireDto = codec.decode( json, ExecutionActivityDTO.class );
        log.debug( "Decoded DTO from json {}", wireDto );
        assertThat( wireDto.getTimestamp(), equalTo( expectedDate ) );
        
        // Fixed in Jackson 1.6.2; regression in 1.6.4
        // See http://jira.codehaus.org/browse/JACKSON-288
    }
}
