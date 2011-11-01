/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
package hudson.util;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.tasks.Mailer;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.util.Map;
import org.hudsonci.model.project.property.ExternalProjectProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

/**
 * Test for DescribableListUtil class
 * </p>
 * Date: 10/20/2011
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Hudson.class, FreeStyleProject.class, Mailer.DescriptorImpl.class})
public class DescribableListUtilTest {

    private Job job;

    @Test
    public void testConvertToProjectProperties1() {
        prepareJob();
        DescribableList<Publisher, Descriptor<Publisher>> list = null;
        Map<String, ExternalProjectProperty<Publisher>> map = DescribableListUtil.convertToProjectProperties(list, job);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testConvertToProjectProperties2() {
        prepareJob();
        DescribableList<Publisher, Descriptor<Publisher>> list
            = new DescribableList<Publisher, Descriptor<Publisher>>();
        Map<String, ExternalProjectProperty<Publisher>> map = DescribableListUtil.convertToProjectProperties(list, job);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testConvertToProjectProperties3() throws IOException {
        Hudson hudson = createMock(Hudson.class);
        Mailer.DescriptorImpl descriptor = createMock(Mailer.DescriptorImpl.class);
        String mailerName = "hudson-tasks-Mailer";
        expect(descriptor.getJsonSafeClassName()).andReturn(mailerName);
        expect(hudson.getDescriptorOrDie(Mailer.class)).andReturn(descriptor);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        prepareJob();

        DescribableList<Publisher, Descriptor<Publisher>> list
            = new DescribableList<Publisher, Descriptor<Publisher>>();

        list.add(new Mailer());

        Map<String, ExternalProjectProperty<Publisher>> map = DescribableListUtil.convertToProjectProperties(list, job);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertNotNull(map.get(mailerName));
        assertEquals(map.get(mailerName).getValue().getClass(), Mailer.class);

    }

    public void prepareJob() {
        job = createMock(FreeStyleProject.class);
        expect(job.hasCascadingProject()).andReturn(false);
        replayAll();
    }
}
