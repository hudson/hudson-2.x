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
 *    Alan Harder
 *     
 *
 *******************************************************************************/ 

package hudson.console;

import hudson.MarkupText;
import junit.framework.TestCase;

/**
 * @author Alan Harder
 */
public class UrlAnnotatorTest extends TestCase {
    public void testAnnotate() {
        ConsoleAnnotator ca = new UrlAnnotator().newInstance(null);
        MarkupText text = new MarkupText("Hello <foo>http://foo/</foo> Bye");
        ca.annotate(null, text);
        assertEquals("Hello &lt;foo><a href='http://foo/'>http://foo/</a>&lt;/foo> Bye",
                     text.toString(true));
        text = new MarkupText("Hello [foo]http://foo/bar.txt[/foo] Bye");
        ca.annotate(null, text);
        assertEquals("Hello [foo]<a href='http://foo/bar.txt'>http://foo/bar.txt</a>[/foo] Bye",
                     text.toString(true));
        text = new MarkupText(
                "Hello 'http://foo' or \"ftp://bar\" or <https://baz/> or (http://a.b.c/x.y) Bye");
        ca.annotate(null, text);
        assertEquals("Hello '<a href='http://foo'>http://foo</a>' or \"<a href='ftp://bar'>"
                + "ftp://bar</a>\" or &lt;<a href='https://baz/'>https://baz/</a>> or (<a "
                + "href='http://a.b.c/x.y'>http://a.b.c/x.y</a>) Bye",
                text.toString(true));
        text = new MarkupText("Fake 'http://foo or \"ftp://bar or <https://baz/ or (http://a.b.c/x.y Bye");
        ca.annotate(null, text);
        assertEquals("Fake '<a href='http://foo'>http://foo</a> or \"<a href='ftp://bar'>"
                + "ftp://bar</a> or &lt;<a href='https://baz/'>https://baz/</a> or (<a "
                + "href='http://a.b.c/x.y'>http://a.b.c/x.y</a> Bye",
                text.toString(true));
    }
}
