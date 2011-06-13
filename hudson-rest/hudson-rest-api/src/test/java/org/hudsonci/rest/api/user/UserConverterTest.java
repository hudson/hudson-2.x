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

package org.hudsonci.rest.api.user;

import org.hudsonci.rest.api.user.UserConverter;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.hudsonci.rest.model.UserDTO;
import hudson.model.User;
import org.junit.Test;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

/**
 *
 * @author plynch
 */
@RunWith(MockitoJUnitRunner.class)
public class UserConverterTest {

    @Mock private User source;

    /**
     * Test of convert method, of class UserConverter.
     */
    @Test
    public void testConvert()
    {
        String name = "test";
        Mockito.when(source.getDescription()).thenReturn(
                "test desc");
        Mockito.when(source.getId()).thenReturn(name);
        Mockito.when(source.getFullName()).thenReturn(
                "Test User");

        UserConverter instance = new UserConverter();
        UserDTO dto = instance.convert(source);

        Assert.assertNotNull(dto);
        Assert.assertEquals(name, dto.getId());
        Assert.assertEquals("test desc", dto.getDescription());
        Assert.assertEquals("Test User", dto.getFullName());
    }

}
