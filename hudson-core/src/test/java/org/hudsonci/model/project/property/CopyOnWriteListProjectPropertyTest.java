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
package org.hudsonci.model.project.property;

import hudson.model.FreeStyleProjectMock;
import hudson.util.CopyOnWriteList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Contains test-cases for {@link CopyOnWriteListProjectProperty}.
 * <p/>
 * Date: 11/4/11
 *
 * @author Nikita Levyankov
 */
@SuppressWarnings("unchecked")
public class CopyOnWriteListProjectPropertyTest {

    private CopyOnWriteListProjectProperty property;

    @Before
    public void setUp() {
        final String propertyKey = "propertyKey";
        FreeStyleProjectMock project = new FreeStyleProjectMock("project");
        property = new CopyOnWriteListProjectProperty(project);
        property.setKey(propertyKey);
    }

    /**
     * Verify constructor
     */
    @Test
    public void testConstructor() {
        try {
            new CopyOnWriteListProjectProperty(null);
            fail("Null should be handled by CopyOnWriteListProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Verify {@link CopyOnWriteListProjectProperty#getDefaultValue()} method.
     */
    @Test
    public void testGetDefaultValue() {
        CopyOnWriteList defaultValue = property.getDefaultValue();
        assertNotNull(defaultValue);
        //Default value should be initialized and stored as original value
        assertTrue(property.getOriginalValue() == defaultValue);
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link CopyOnWriteListProjectProperty#getOriginalValue()} method.
     */
    @Test
    public void testGetOriginalValue() {
        //Original value is not initialized. Default value will be used instead. Shouldn't be null
        CopyOnWriteList originalValue = property.getOriginalValue();
        assertNotNull(originalValue);
        //Value was set, so return it without modification
        assertTrue(originalValue == property.getOriginalValue());
    }

    /**
     * Verify {@link CopyOnWriteListProjectProperty#returnOriginalValue()} method.
     */
    @Test
    public void testReturnOriginalValue() {
        //Return cascading is property is not overridden and is empty.
        assertFalse(property.returnOriginalValue());

        //Return properties' originalValue if it was overridden
        property.setOverridden(true);
        assertTrue(property.returnOriginalValue());
        //If property has not empty value - return it (basically for non-cascadable projects)
        property.setOriginalValue(new CopyOnWriteList(Arrays.asList(new Object())), false);
        assertTrue(property.returnOriginalValue());
        //If property has not empty value and was overridden - return it
        property.setOriginalValue(new CopyOnWriteList(Arrays.asList(new Object())), true);
        assertTrue(property.returnOriginalValue());
    }

    /**
     * Verify {@link CopyOnWriteListProjectProperty#clearOriginalValue(hudson.util.CopyOnWriteList)} method.
     */
    @Test
    public void testClearOriginalValue() {
        //Overridden flag should be cleared to false. Pre-set true value
        property.setOverridden(true);
        assertTrue(property.isOverridden());
        CopyOnWriteList originalValue = new CopyOnWriteList(Arrays.asList(new Object()));
        property.clearOriginalValue(originalValue);
        //Original value should be set with overridden flag == false
        assertFalse(property.isOverridden());
        assertTrue(originalValue == property.getOriginalValue());
    }
}
