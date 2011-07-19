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

package org.eclipse.hudson.rest.api.user;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.eclipse.hudson.rest.api.user.UserConverter;
import org.eclipse.hudson.rest.model.UserDTO;
import hudson.model.User;
import org.junit.Test;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

/**
 *
 * @author plynch
 */
@RunWith(MockitoJUnitRunner.class)
public class UserConverterTest {

    @Mock private User source;

    /**
     * Test of convert method, of class UserConverter.
     */
    @Test
    public void testConvert()
    {
        String name = "test";
        Mockito.when(source.getDescription()).thenReturn(
                "test desc");
        Mockito.when(source.getId()).thenReturn(name);
        Mockito.when(source.getFullName()).thenReturn(
                "Test User");

        UserConverter instance = new UserConverter();
        UserDTO dto = instance.convert(source);

        Assert.assertNotNull(dto);
        Assert.assertEquals(name, dto.getId());
        Assert.assertEquals("test desc", dto.getDescription());
        Assert.assertEquals("Test User", dto.getFullName());
    }

}
