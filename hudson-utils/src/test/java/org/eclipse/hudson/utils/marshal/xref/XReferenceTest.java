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

package org.eclipse.hudson.utils.marshal.xref;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.eclipse.hudson.utils.marshal.xref.XReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Tests for {@link XReference}.
 */
public class XReferenceTest
{
    private XStream xs;

    @Before
    public void setUp() throws Exception {
        xs = new XStream();
        xs.autodetectAnnotations(true);
    }

    @After
    public void tearDown() throws Exception {
        xs = null;
    }

    @Test
    public void testMarshal() throws Exception {
        Date value1 = new Date();
        TestRef ref1 = new TestRef(value1, "a");

        String xml = xs.toXML(ref1);
        System.out.println("XML:\n" + xml);
    }

    @XStreamAlias("test-ref")
    private static class TestRef
        extends XReference
    {
        private String path;

        private TestRef(Object value, String path) {
            super(value);
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }
    }
}
