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

package org.hudsonci.rest.common;

import org.hudsonci.rest.model.build.BuildEventDTO;
import org.hudsonci.rest.model.build.BuildEventTypeDTO;

import org.hudsonci.rest.common.JacksonCodec;
import org.hudsonci.rest.common.JsonCodec;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link JacksonCodec}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class JacksonCodecTest
{
    private JsonCodec codec;

    @Before
    public void setUp() throws Exception {
        codec = new JacksonCodec();
    }

    @Test
    public void testSerialize() throws Exception {
        BuildEventDTO event = new BuildEventDTO();
        event.setType(BuildEventTypeDTO.STARTED);
        event.setProjectName("foo");
        event.setBuildNumber(1);

        String value = codec.encode(event);
        System.out.println("VALUE: " + value);

        BuildEventDTO event2 = codec.decode(value, BuildEventDTO.class);
        System.out.println("EVENT: " + event2);

        assertEquals(event.getType(), event2.getType());
        assertEquals(event.getProjectName(), event2.getProjectName());
        assertEquals(event.getBuildNumber(), event2.getBuildNumber());
    }
}
