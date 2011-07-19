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

package org.eclipse.hudson.maven.plugin.builder.rest;

import org.eclipse.hudson.rest.common.JacksonCodec;
import org.eclipse.hudson.maven.model.state.ExecutionActivityDTO;
import org.eclipse.hudson.maven.model.state.ExecutionActivityTypeDTO;
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
