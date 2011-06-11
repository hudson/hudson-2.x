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

import org.hudsonci.maven.model.PropertiesDTOHelper;
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
