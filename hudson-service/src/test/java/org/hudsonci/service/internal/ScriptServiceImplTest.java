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

package org.hudsonci.service.internal;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import hudson.model.Hudson;
import hudson.model.Hudson.MasterComputer;
import hudson.util.RemotingDiagnostics;

import org.hudsonci.service.SecurityService;
import org.hudsonci.service.internal.ScriptServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Hudson.class, MasterComputer.class, RemotingDiagnostics.class })
public class ScriptServiceImplTest {

    private Hudson hudson;

    private ScriptServiceImpl scriptService;

    @Mock
    private SecurityService security;

    @Before
    public void setUp() throws Exception {
        mockStatic(Hudson.class); // static methods
        hudson = mock(Hudson.class); // final and native

        mockStatic(RemotingDiagnostics.class);
        mockStatic(MasterComputer.class);

        MockitoAnnotations.initMocks(this);
        scriptService = new ScriptServiceImpl(security);
        scriptService.setHudson(hudson);
    }

    private ScriptServiceImpl getInst() {
        return scriptService;
    }

    @Test
    public void setupProperly() {
        assertThat(getInst(), notNullValue());
        assertThat(hudson, notNullValue());
    }

    @Test(expected = NullPointerException.class)
    public void executeArgNull() throws Exception {
        getInst().execute(null);
    }

    @Test
    public void executeSecurity() throws Exception {
        getInst().execute("String bogus");
        Mockito.verify(security).checkPermission(Hudson.ADMINISTER);
    }

}
