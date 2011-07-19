/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import junit.framework.TestCase;

import java.security.SecureRandom;

import hudson.Util;
import hudson.model.Hudson;

/**
 * @author Kohsuke Kawaguchi
 */
public class SecretTest extends TestCase {
    @Override protected void setUp() throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] random = new byte[32];
        sr.nextBytes(random);
        Secret.SECRET = Util.toHexString(random);

    }

    @Override protected void tearDown() throws Exception {
        Secret.SECRET = null;
    }

    public void testEncrypt() {
        Secret secret = Secret.fromString("abc");
        assertEquals("abc",secret.getPlainText());

        // make sure we got some encryption going
        System.out.println(secret.getEncryptedValue());
        assertTrue(!"abc".equals(secret.getEncryptedValue()));

        // can we round trip?
        assertEquals(secret,Secret.fromString(secret.getEncryptedValue()));
    }

    public void testDecrypt() {
        assertEquals("abc",Secret.toString(Secret.fromString("abc")));
    }

    public void testSerialization() {
        Secret s = Secret.fromString("Mr.Hudson");
        String xml = Hudson.XSTREAM.toXML(s);
        assertTrue(xml, !xml.contains(s.getPlainText()));
        assertTrue(xml, xml.contains(s.getEncryptedValue()));
        Object o = Hudson.XSTREAM.fromXML(xml);
        assertEquals(xml, o, s);
    }

    public static class Foo {
        Secret password;
    }

    /**
     * Makes sure the serialization form is backward compatible with String.
     */
    public void testCompatibilityFromString() {
        String tagName = Foo.class.getName().replace("$","_-");
        String xml = "<"+tagName+"><password>secret</password></"+tagName+">";
        Foo foo = new Foo();
        Hudson.XSTREAM.fromXML(xml, foo);
        assertEquals("secret",Secret.toString(foo.password));
    }
}
