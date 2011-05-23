/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import com.sonatype.matrix.maven.model.state.ExecutionActivityDTO;
import com.sonatype.matrix.maven.model.state.ExecutionActivityTypeDTO;
import com.sonatype.matrix.rest.common.JacksonCodec;
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
