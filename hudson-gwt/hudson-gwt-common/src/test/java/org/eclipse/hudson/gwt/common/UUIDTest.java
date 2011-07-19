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

package org.eclipse.hudson.gwt.common;

import org.eclipse.hudson.gwt.common.UUID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link UUID}.
 */
public class UUIDTest
{
    @Test
    public void testJavaUtilUUIDInterop() {
        String uuid1 = UUID.uuid();
        java.util.UUID uuid2 = java.util.UUID.fromString(uuid1);
        assertEquals(uuid1, uuid2.toString());
    }
}
