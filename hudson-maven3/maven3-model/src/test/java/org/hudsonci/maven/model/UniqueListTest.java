/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.model;

import com.thoughtworks.xstream.XStream;

import org.hudsonci.maven.model.UniqueList;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link UniqueList}.
 */
public class UniqueListTest
{
    @Test
    public void testAddDuplicates() {
        UniqueList<String> list = new UniqueList<String>();
        list.add("foo");
        list.add("bar");
        assertEquals(2, list.size());

        list.add("bar");
        assertEquals(2, list.size());
    }

    @Test
    public void testRemove() {
        UniqueList<String> list = new UniqueList<String>();
        list.add("foo");
        list.add("bar");
        assertEquals(2, list.size());

        list.remove("bar");
        assertEquals(1, list.size());
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testSerializationViaXStream() throws Exception {
        XStream xs = new XStream();
        xs.processAnnotations(UniqueList.class);

        UniqueList<String> list1 = new UniqueList<String>();
        list1.add("foo");
        list1.add("bar");

        String xml = xs.toXML(list1);

        UniqueList<String> list2 = (UniqueList<String>) xs.fromXML(xml);
        assertEquals(list1, list2);
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testSerializationViaXStreamWithManualDuplicates() throws Exception {
        XStream xs = new XStream();
        xs.processAnnotations(UniqueList.class);

        String xml = "<unique-list>\n" +
                "  <string>foo</string>\n" +
                "  <string>foo</string>\n" +
                "  <string>foo</string>\n" +
                "  <string>bar</string>\n" +
                "  <string>bar</string>\n" +
                "</unique-list>";

        UniqueList<String> list = (UniqueList<String>) xs.fromXML(xml);
        assertEquals(2, list.size());
        assertEquals("foo", list.get(0));
        assertEquals("bar", list.get(1));
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testArtifactListSerialization() throws Exception {
        XStream xs = new XStream();
        xs.processAnnotations(new Class[] {UniqueList.class, ArtifactDTO.class});

        UniqueList<ArtifactDTO> list1 = new UniqueList<ArtifactDTO>();
        list1.add(new ArtifactDTO().withCoordinates(new MavenCoordinatesDTO().withGroupId("gid").withArtifactId("aid")));

        String xml = xs.toXML(list1);
        UniqueList<ArtifactDTO> list2 = (UniqueList<ArtifactDTO>) xs.fromXML(xml);
        assertEquals(list1, list2);
    }
}
