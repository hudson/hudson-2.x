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

import hudson.model.BooleanParameterDefinition;
import hudson.model.FreeStyleProjectMock;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;


/**
 * Test cases for {@link CascadingUtil#setParameterDefinitionProperties(hudson.model.Job, String, CopyOnWriteList)}
 * method.
 * Date: 11/8/11
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Hudson.class})
public class CascadingParameterDefinitionPropertiesTest {
    private FreeStyleProjectMock p1, p2, p3;
    private String propertyKey;
    private CopyOnWriteList<ParametersDefinitionProperty> copyOnWriteList;

    @Before
    public void setUp() throws Exception {
        p1 = new FreeStyleProjectMock("p1");
        p2 = new FreeStyleProjectMock("p2");
        p3 = new FreeStyleProjectMock("p3");
        p2.setCascadingProject(p1);
        CascadingUtil.linkCascadingProjectsToChild(p1, "p2");
        p3.setCascadingProject(p2);
        CascadingUtil.linkCascadingProjectsToChild(p2, "p3");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getItem("p2")).andReturn(p2).anyTimes();
        expect(hudson.getItem("p3")).andReturn(p3).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replay(Hudson.class, hudson);
        propertyKey = Job.PARAMETERS_DEFINITION_JOB_PROPERTY_PROPERTY_NAME;
        List<ParameterDefinition> parameterDefinitionList = new ArrayList<ParameterDefinition>();
        parameterDefinitionList.add(new BooleanParameterDefinition("name", true, "d1"));
        ParametersDefinitionProperty property = new ParametersDefinitionProperty(parameterDefinitionList);
        copyOnWriteList = new CopyOnWriteList<ParametersDefinitionProperty>();
        copyOnWriteList.add(property);
    }

    /**
     * Tests main functionality of setParameterDefinitionProperties() method.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetParameterDefinitionProperties() throws Exception {
        assertEquals(new CopyOnWriteList(),
            CascadingUtil.getCopyOnWriteListProjectProperty(p1, propertyKey).getValue());
        CascadingUtil.setParameterDefinitionProperties(p1, propertyKey, copyOnWriteList);
        assertEquals(copyOnWriteList, CascadingUtil.getCopyOnWriteListProjectProperty(p1, propertyKey).getValue());
        verifyAll();
    }

    /**
     * Tests recursively setting  parameters definition property.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetParameterDefinitionPropertiesRecursively() throws Exception {
        assertEquals(new CopyOnWriteList(),
            CascadingUtil.getCopyOnWriteListProjectProperty(p2, propertyKey).getValue());
        CascadingUtil.setParameterDefinitionProperties(p1, propertyKey, copyOnWriteList);
        assertEquals(copyOnWriteList, CascadingUtil.getCopyOnWriteListProjectProperty(p2, propertyKey).getValue());
        verifyAll();
    }

    /**
     * Tests if parameter definition property correctly marked as overridden.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetParameterDefinitionPropertiesOverridden() throws Exception {
        CascadingUtil.setParameterDefinitionProperties(p1, propertyKey, copyOnWriteList);
        CascadingUtil.setParameterDefinitionProperties(p2, propertyKey,
            new CopyOnWriteList<ParametersDefinitionProperty>());
        assertTrue(CascadingUtil.getCopyOnWriteListProjectProperty(p2, propertyKey).isOverridden());
        assertFalse(CascadingUtil.getCopyOnWriteListProjectProperty(p3, propertyKey).isOverridden());
    }

    /**
     * Tests correctly behaviour when job hasn't children..
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetParameterDefinitionPropertiesEmptyChildren() throws Exception {
        CascadingUtil.setParameterDefinitionProperties(p3, propertyKey, copyOnWriteList);
        assertEquals(copyOnWriteList, CascadingUtil.getCopyOnWriteListProjectProperty(p3, propertyKey).getValue());
    }

    /**
     * Tests if trigger property marked as non overridden when its value match the cascading value.
     *
     * @throws Exception if any.
     */
    @Test
    public void testSetParameterDefinitionPropertiesOverriddenFalse() throws Exception {
        CascadingUtil.getCopyOnWriteListProjectProperty(p2, propertyKey).setValue(copyOnWriteList);
        CascadingUtil.setParameterDefinitionProperties(p1, propertyKey, copyOnWriteList);
        assertFalse(CascadingUtil.getCopyOnWriteListProjectProperty(p2, propertyKey).isOverridden());
    }
}
