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

package org.eclipse.hudson.utils.common;

import org.eclipse.hudson.utils.common.Iso8601Date;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link Iso8601Date}.
 */
public class Iso8601DateTest
{
    @Test
    public void testFormatParse() throws Exception {
        Date date1 = new Date();

        String formatted = Iso8601Date.format(date1);
        assertNotNull(formatted);

        Date date2 = Iso8601Date.parse(formatted);
        assertNotNull(date2);

        assertEquals(date1.getTime(), date2.getTime());
    }
}
