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

package org.eclipse.hudson.rest.api.status;

import org.eclipse.hudson.rest.api.status.StatusConverter;
import org.eclipse.hudson.rest.model.InitLevelDTO;
import hudson.init.InitMilestone;
import hudson.util.VersionNumber;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link StatusConverter}
 * @author plynch
 */
public class StatusConverterTest {

    @Test
    public void testConvert_VersionNumber()
    {
        StatusConverter statusx = new StatusConverter();
        VersionNumber source = new VersionNumber("2.1.0-SNAPSHOT");
        assertEquals("2.1.-1.1000", statusx.convert(source));
    }

    @Test
    public void testConvert_InitMilestone()
    {
        StatusConverter statusx = new StatusConverter();
        assertEquals(InitLevelDTO.COMPLETED, statusx.convert(InitMilestone.COMPLETED));
    }

}
