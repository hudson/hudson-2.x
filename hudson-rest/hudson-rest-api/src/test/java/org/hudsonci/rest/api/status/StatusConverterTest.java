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

package org.hudsonci.rest.api.status;

import org.hudsonci.rest.model.InitLevelDTO;
import hudson.init.InitMilestone;
import hudson.util.VersionNumber;

import org.hudsonci.rest.api.status.StatusConverter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link StatusConverter}
 * @author plynch
 */
public class StatusConverterTest {

    @Test
    public void testConvert_VersionNumber()
    {
        StatusConverter statusx = new StatusConverter();
        VersionNumber source = new VersionNumber("2.1.0-SNAPSHOT");
        assertEquals("2.1.-1.1000", statusx.convert(source));
    }

    @Test
    public void testConvert_InitMilestone()
    {
        StatusConverter statusx = new StatusConverter();
        assertEquals(InitLevelDTO.COMPLETED, statusx.convert(InitMilestone.COMPLETED));
    }

}
