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

import com.thoughtworks.xstream.XStream;

import org.eclipse.hudson.maven.model.ThrowableDTOHelper;
import org.eclipse.hudson.maven.model.ThrowableDTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ThrowableDTOHelper}.
 */
public class ThrowableDTOHelperTest
{
    @Test
    public void testThrowable() throws Exception {
        Throwable source = new Throwable("hello");
        ThrowableDTO throwable1 = ThrowableDTOHelper.convert(source);
        assertEquals(Throwable.class.getName(), throwable1.getType());
        assertEquals(source.getMessage(), throwable1.getMessage());

        XStream xs = new XStream();
        xs.autodetectAnnotations(true);

        String xml = xs.toXML(throwable1);
        System.out.println("XML:\n" + xml);

        ThrowableDTO throwable2 = (ThrowableDTO) xs.fromXML(xml);
        assertEquals(Throwable.class.getName(), throwable2.getType());
        assertEquals(source.getMessage(), throwable2.getMessage());
    }
}
