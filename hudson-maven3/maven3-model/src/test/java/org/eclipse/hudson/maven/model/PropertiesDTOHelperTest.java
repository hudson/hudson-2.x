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

import org.eclipse.hudson.maven.model.PropertiesDTOHelper;
import org.eclipse.hudson.maven.model.PropertiesDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link PropertiesDTOHelper}.
 */
public class PropertiesDTOHelperTest
{
    @Test
    public void testConvertProperties() throws Exception {
        Properties source = new Properties();
        source.put("a", "b");
        source.put("c", "d");

        PropertiesDTO props1 = PropertiesDTOHelper.convert(source);
        assertNotNull(props1.getEntries());
        Assert.assertEquals(2, props1.getEntries().size());

        XStream xs = new XStream();
        xs.autodetectAnnotations(true);

        String xml = xs.toXML(props1);
        System.out.println("XML:\n" + xml);

        PropertiesDTO props2 = (PropertiesDTO) xs.fromXML(xml);
        assertNotNull(props2.getEntries());
        Assert.assertEquals(2, props2.getEntries().size());
    }
}
