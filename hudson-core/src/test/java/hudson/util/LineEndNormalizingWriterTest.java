/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
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

import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class LineEndNormalizingWriterTest extends TestCase {
    public void test1() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = new LineEndNormalizingWriter(sw);

        w.write("abc\r\ndef\r");
        w.write("\n");

        assertEquals(sw.toString(),"abc\r\ndef\r\n");
    }

    public void test2() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = new LineEndNormalizingWriter(sw);

        w.write("abc\ndef\n");
        w.write("\n");

        assertEquals(sw.toString(),"abc\r\ndef\r\n\r\n");
    }

    public void test3() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = new LineEndNormalizingWriter(sw);

        w.write("\r\n\n");

        assertEquals(sw.toString(),"\r\n\r\n");
    }
}
