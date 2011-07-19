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

package org.eclipse.hudson.utils.id;

import org.eclipse.hudson.utils.id.OID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link OID}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class OIDTest
{
    @Test
    public void testSimple() {
        Object obj = new Object();
        OID oid = OID.get(obj);
        assertEquals(obj.toString(), oid.toString());
    }

    @Test
    public void testParse() {
        Object obj = new Object();
        String spec = obj.toString();
        OID oid = OID.parse(spec);
        assertEquals(obj.getClass().getName(), oid.getType());
        assertEquals(obj.hashCode(), oid.getHash());
        assertEquals(spec, oid.toString());
    }

    @Test
    public void testGetNull() {
        OID oid = OID.get(null);
        assertEquals(OID.NULL, oid);
    }
}
