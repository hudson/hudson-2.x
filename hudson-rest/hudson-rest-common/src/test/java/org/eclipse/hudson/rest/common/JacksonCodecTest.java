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

package org.eclipse.hudson.rest.common;

import org.eclipse.hudson.rest.common.JacksonCodec;
import org.eclipse.hudson.rest.common.JsonCodec;
import org.eclipse.hudson.rest.model.build.BuildEventDTO;
import org.eclipse.hudson.rest.model.build.BuildEventTypeDTO;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link JacksonCodec}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class JacksonCodecTest
{
    private JsonCodec codec;

    @Before
    public void setUp() throws Exception {
        codec = new JacksonCodec();
    }

    @Test
    public void testSerialize() throws Exception {
        BuildEventDTO event = new BuildEventDTO();
        event.setType(BuildEventTypeDTO.STARTED);
        event.setProjectName("foo");
        event.setBuildNumber(1);

        String value = codec.encode(event);
        System.out.println("VALUE: " + value);

        BuildEventDTO event2 = codec.decode(value, BuildEventDTO.class);
        System.out.println("EVENT: " + event2);

        assertEquals(event.getType(), event2.getType());
        assertEquals(event.getProjectName(), event2.getProjectName());
        assertEquals(event.getBuildNumber(), event2.getBuildNumber());
    }
}
