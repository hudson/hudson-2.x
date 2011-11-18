/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak
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

import hudson.model.FreeStyleProjectMock;
import hudson.model.Hudson;
import hudson.triggers.TimerTrigger;
import hudson.triggers.TriggerDescriptor;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Test cases for {@link CascadingUtil#setChildrenTrigger(hudson.model.Job, hudson.triggers.TriggerDescriptor, String,
 * org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)} method.
 *
 * Date: 11/8/11
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Hudson.class, StaplerRequest.class})
public class CascadingTriggersTest {
    private FreeStyleProjectMock p1, p2, p3;
    private String propertyKey;
    private TriggerDescriptor descriptor;
    private JSONObject json;
    private StaplerRequest req;

    @Before
    public void setUp() throws Exception {
        p1 = new FreeStyleProjectMock("p1");
        p2 = new FreeStyleProjectMock("p2");
        p3 = new FreeStyleProjectMock("p3");
        p2.setCascadingProject(p1);
        CascadingUtil.linkCascadingProjectsToChild(p1, "p2");
        p3.setCascadingProject(p2);
        CascadingUtil.linkCascadingProjectsToChild(p2, "p3");
        descriptor = new TimerTrigger.DescriptorImpl();
        propertyKey = descriptor.getJsonSafeClassName();
        req = createMock(StaplerRequest.class);
        json = new JSONObject();
        JSONObject refspec = new JSONObject();
        refspec.put("refspec", "* * * * *");
        json.put(propertyKey, refspec);
        expect(req.bindJSON(TimerTrigger.class, refspec)).andReturn(new TimerTrigger("* * * * *")).anyTimes();
        mockStatic(Hudson.class);
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getDescriptorOrDie(TimerTrigger.class)).andReturn(descriptor).anyTimes();
        expect(hudson.getItem("p2")).andReturn(p2).anyTimes();
        expect(hudson.getItem("p3")).andReturn(p3).anyTimes();
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replay(Hudson.class, hudson, req);
    }

    /**
     * Tests main functionality of setChildrenTrigger() method.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetChildrenTrigger() throws Exception {
        assertNull(CascadingUtil.getTriggerProjectProperty(p1, propertyKey).getValue());
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        assertNotNull(CascadingUtil.getTriggerProjectProperty(p1, propertyKey).getValue());
        verifyAll();
    }

    /**
     * Tests recursively setting trigger property,
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetChildrenTriggerRecursively() throws Exception {
        assertNull(CascadingUtil.getTriggerProjectProperty(p2, propertyKey).getValue());
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        assertNotNull(CascadingUtil.getTriggerProjectProperty(p2, propertyKey).getValue());
        assertEquals(CascadingUtil.getTriggerProjectProperty(p1, propertyKey).getValue(),
            CascadingUtil.getTriggerProjectProperty(p2, propertyKey).getValue());
        verifyAll();
    }

    /**
     * Tests if trigger property correctly marked as overridden.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetChildrenTriggerOverridden() throws Exception {
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        CascadingUtil.getTriggerProjectProperty(p2, propertyKey).setValue(new TimerTrigger("*/2 * * * *"));
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        assertTrue(CascadingUtil.getTriggerProjectProperty(p2, propertyKey).isOverridden());
        verifyAll();
    }

    /**
     * Tests if trigger property value is null if there is no json data.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetChildrenTriggerEmptyJSON() throws Exception {
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, new JSONObject());
        assertNull(CascadingUtil.getTriggerProjectProperty(p1, propertyKey).getValue());
        verifyAll();
    }

    /**
     * Tests correctly behaviour if job hasn't children.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetChildrenTriggerEmptyChildren() throws Exception {
        CascadingUtil.setChildrenTrigger(p3, descriptor, propertyKey, req, json);
        assertNotNull(CascadingUtil.getTriggerProjectProperty(p3, propertyKey).getValue());
        verifyAll();
    }

    /**
     * Tests if trigger property marked as non overridden when its value match the cascading value.
     *
     * @throws Exception if any
     */
    @Test
    public void testSetChildrenTriggerOverridenFalse() throws Exception {
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        CascadingUtil.getTriggerProjectProperty(p2, propertyKey).setValue(new TimerTrigger("*/2 * * * *"));
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        assertTrue(CascadingUtil.getTriggerProjectProperty(p2, propertyKey).isOverridden());
        CascadingUtil.getTriggerProjectProperty(p2, propertyKey).setValue(new TimerTrigger("* * * * *"));
        CascadingUtil.setChildrenTrigger(p1, descriptor, propertyKey, req, json);
        assertFalse(CascadingUtil.getTriggerProjectProperty(p2, propertyKey).isOverridden());
        verifyAll();
    }
}
