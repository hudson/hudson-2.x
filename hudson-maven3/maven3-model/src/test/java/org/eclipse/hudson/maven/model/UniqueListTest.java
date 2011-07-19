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

import org.eclipse.hudson.maven.model.UniqueList;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
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
