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

import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.model.FreeStyleProjectMock;
import hudson.tasks.JavadocArchiver;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Contains test-cases for {@link BaseProjectProperty}.
 * <p/>
 * Date: 11/4/11
 *
 * @author Nikita Levyankov
 */
@SuppressWarnings("unchecked")
public class BaseProjectPropertyTest {
    private final String propertyKey = "propertyKey";

    private BaseProjectProperty property;
    private BaseProjectProperty parentProperty;
    private FreeStyleProjectMock project;
    private FreeStyleProjectMock parent;
    private String parentPropertyValue = "parentValue";

    @Before
    public void setUp() {
        parent = new FreeStyleProjectMock("parent");
        project = new FreeStyleProjectMock("project");

        property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        parentProperty = new BaseProjectProperty(parent);
        parentProperty.setKey(propertyKey);
        parentProperty.setValue(parentPropertyValue);
        parent.putProjectProperty(propertyKey, parentProperty);
    }

    /**
     * Verify constructor
     */
    @Test
    public void testConstructor() {
        try {
            new BaseProjectProperty(null);
            fail("Null should be handled by BaseProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        assertNotNull(property.getJob());
        assertEquals(project, property.getJob());
    }

    /**
     * Verify {@link BaseProjectProperty#setJob(org.hudsonci.api.model.IJob)} method.
     */
    @Test
    public void testSetJob() {
        try {
            property.setJob(null);
            fail("Null is not allowed.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        assertNotNull(property.getJob());
        assertEquals(project, property.getJob());
    }

    /**
     * Verify {@link BaseProjectProperty#prepareValue(Object)} method.
     */
    @Test
    public void testPrepareValue() {
        //BaseProject property doesn't perform any changes with value.
        assertNull(property.prepareValue(null));
        Object value = new Object();
        assertTrue(value == property.prepareValue(value));
    }


    /**
     * Verify {@link BaseProjectProperty#getDefaultValue()} method.
     */
    @Test
    public void testGetDefaultValue() {
        assertNull(property.getDefaultValue());
    }

    /**
     * Verify {@link BaseProjectProperty#allowOverrideValue(Object, Object)} method.
     */
    @Test
    public void testAllowOverrideValue() {
        assertFalse(property.allowOverrideValue(null, null));
        assertTrue(property.allowOverrideValue(new Object(), null));
        assertTrue(property.allowOverrideValue(null, new Object()));
        //Test properties that don't have correct equals methods
        assertFalse(property.allowOverrideValue(new JavadocArchiver("", false), new JavadocArchiver("", false)));
        assertTrue(property.allowOverrideValue(new JavadocArchiver("", true), new JavadocArchiver("", false)));
        //Object with transient filds should be taken into account
        assertTrue(property.allowOverrideValue(new AxisList(new Axis("DB", "mysql")), new AxisList()));
        assertTrue(property.allowOverrideValue(new AxisList(new Axis("DB", "mysql")),
            new AxisList(new Axis("DB", "mysql", "mssql"))));
        assertTrue(property.allowOverrideValue(new AxisList(new Axis("DB", "mysql")),
            new AxisList(new Axis("DB", "mssql"))));
    }

    /**
     * Verify {@link BaseProjectProperty#getCascadingValue()} method.
     */
    @Test
    public void testGetCascadingValue() {
        parentProperty.setValue(null);
        //If project doesn't have cascading project - default value is used as cascading value.
        assertEquals(property.getDefaultValue(), property.getCascadingValue());

        project.setCascadingProject(parent);
        property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        //If project has cascading project and cascading value is not set - default value is used.
        assertEquals(property.getDefaultValue(), property.getCascadingValue());

        project.setCascadingProject(parent);
        property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(parentPropertyValue);
        //If project has cascading project and cascading value is set - property value will be used.
        assertEquals(parentProperty.getOriginalValue(), property.getCascadingValue());
    }

    /**
     * Verify {@link BaseProjectProperty#getOriginalValue()} method.
     */
    @Test
    public void testGetOriginalValue() {
        assertNull(property.getOriginalValue());
        Object value = new Object();
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
        property.setValue(null);
        assertNull(property.getOriginalValue());
    }

    /**
     * Property should have not null property key.
     */
    @Test
    public void testNullPropertyKey() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        try {
            property.setValue("value");
            fail("PropertyKey shouldn't be null while calling setValue");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_PROPERTY_KEY_EXCEPTION, e.getMessage());
        }
        try {
            property.getCascadingValue();
            fail("PropertyKey shouldn't be null while calling getCascadingValue()");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_PROPERTY_KEY_EXCEPTION, e.getMessage());
        }
        try {
            property.getValue();
            fail("PropertyKey shouldn't be null while calling getValue()");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_PROPERTY_KEY_EXCEPTION, e.getMessage());
        }

        property.setKey("key");
        try {
            property.setValue("value");
            property.getCascadingValue();
            property.getValue();
        } catch (Exception e) {
            fail("PropertyKey is valid");
        }
    }

    /**
     * Verify {@link BaseProjectProperty#resetValue()} method.
     */
    @Test
    public void testResetValue() {
        property.setKey(propertyKey);
        property.setValue(new Object());
        property.setOverridden(true);
        assertNotNull(property.getOriginalValue());
        assertTrue(property.isOverridden());
        property.resetValue();
        assertNull(property.getOriginalValue());
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#setOverridden(boolean)}, {@link BaseProjectProperty#isOverridden()} methods.
     */
    @Test
    public void testGetSetOverridden() {
        //By default property isOverridden flag is set to false.
        assertFalse(property.isOverridden());
        //Test if flag is configured as expected. Set true/false values and check whether they are set correctly.
        property.setOverridden(true);
        assertTrue(property.isOverridden());
        property.setOverridden(false);
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#returnOriginalValue()} method.
     */
    @Test
    public void testReturnOriginalValue() {
        //False - If isOverridden flag is false and original value is null
        assertFalse(property.returnOriginalValue());
        property.setOverridden(true);

        //True - If isOverridden flag is true or original value is not null
        assertTrue(property.returnOriginalValue());
        property.setOriginalValue(new Object(), false);
        assertTrue(property.returnOriginalValue());
    }

    /**
     * Verify {@link BaseProjectProperty#setValue(Object)} method.
     */
    @Test
    public void testSetValue() {
        //If project doesn't have cascading - default boolean value is used for propertyOverridden flag
        assertFalse(property.isOverridden());
        assertNull(property.getOriginalValue());

        Object value = 12345;
        property.setValue(value);
        //If project doesn't have cascading - default boolean value is used for propertyOverridden flag
        assertFalse(property.isOverridden());
        assertEquals(value, property.getOriginalValue());

        project.setCascadingProject(parent);

        //If value set to null, need to check whether default value is equals to cascading
        property.setValue(null);
        assertTrue(property.isOverridden());
        String overriddenValue = "newValue";
        property.setValue(overriddenValue);
        assertTrue(property.isOverridden());

        //Check whether current value is not null, after setting equal-to-cascading value current will be null
        assertNotNull(property.getOriginalValue());
        assertTrue(property.isOverridden());
        property.setValue(parentPropertyValue);
        //Reset current property to null
        assertNull(property.getOriginalValue());
        //Cascading value is equal to current - reset flag to false.
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#setOriginalValue(Object, boolean)} method.
     */
    @Test
    public void testSetOriginalValue() {
        Object originalValue = new Object();
        //Prepare property original value and mark it as overridden
        property.setOriginalValue(originalValue, true);
        assertEquals(originalValue, property.getOriginalValue());
        assertTrue(property.isOverridden());

        //Prepare property original value and set overridden flag to false.
        originalValue = new Object();
        property.setOriginalValue(originalValue, false);
        assertEquals(originalValue, property.getOriginalValue());
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#clearOriginalValue(Object)} method.
     */
    @Test
    public void testClearOriginalValue() {
        Object originalValue = new Object();
        //Prepare property original value
        property.setOriginalValue(originalValue, true);
        assertEquals(originalValue, property.getOriginalValue());
        assertTrue(property.isOverridden());
        property.clearOriginalValue(null);
        //original value should be null and not overridden.
        assertNull(property.getOriginalValue());
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#updateOriginalValue(Object, Object)} method.
     */
    @Test
    public void testUpdateOriginalValue() {
        Integer cascadingValue = 10;
        Integer candidateValue = 11;
        property.setOriginalValue(1, false);
        assertFalse(property.isOverridden());
        assertNotNull(property.getOriginalValue());
        //If candidate value is not equal to cascading value - update original value and mark it as overridden
        boolean result = property.updateOriginalValue(candidateValue, cascadingValue);
        assertTrue(result);
        assertTrue(property.isOverridden());
        assertEquals(candidateValue, property.getOriginalValue());

        //If candidate value is equal to cascading value - clear original value and mark it non-overridden
        result = property.updateOriginalValue(cascadingValue, cascadingValue);
        assertFalse(result);
        assertFalse(property.isOverridden());
        assertNull(property.getOriginalValue());


    }

    /**
     * Verify {@link BaseProjectProperty#getValue()} method.
     */
    @Test
    public void testGetValue() {
        property.setValue(parentPropertyValue);
        //if value is not null - return it
        assertEquals(parentPropertyValue, property.getValue());
        property.setValue(null);
        assertNull(property.getValue());

        parentProperty.setValue(parentPropertyValue);
        project.setCascadingProject(parent);
        property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        //if current value is null and is not overridden value, take from cascading
        assertNull(property.getOriginalValue());
        assertEquals(parentPropertyValue, property.getValue());

        property.setOverridden(true);
        //Property is overridden - return current value even if it is null.
        assertNull(property.getOriginalValue());
        assertNull(property.getValue());
    }

    /**
     * Verify {@link BaseProjectProperty#onCascadingProjectRemoved()} method.
     */
    @Test
    public void testOnCascadingProjectRemoved() {
        //Overridden flag should be set to false when cascading project was removed
        property.setOverridden(true);
        property.onCascadingProjectRemoved();
        assertFalse(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#onCascadingProjectRemoved()} method.
     */
    @Test
    public void testOnCascadingProjectSet() {
        property.setValue(parentPropertyValue);
        project.setCascadingProject(parent);
        //If parent value equals to current value - isOverridden flag should be set to false.
        property.setOverridden(true);
        assertTrue(property.isOverridden());
        assertFalse(property.allowOverrideValue(parentProperty.getOriginalValue(), property.getValue()));
        property.onCascadingProjectSet();
        assertFalse(property.isOverridden());

        property.setValue("newValue");
        assertTrue(property.allowOverrideValue(parentProperty.getOriginalValue(), property.getValue()));
        property.onCascadingProjectSet();
        assertTrue(property.isOverridden());
    }

    /**
     * Verify {@link BaseProjectProperty#onCascadingProjectChanged()} method.
     */
    @Test
    public void testOnCascadingProjectChanged() {
        //Overridden flag should be set to false when cascading project was removed
        property.setOverridden(true);
        property.onCascadingProjectChanged();
        assertFalse(property.isOverridden());

        property.setValue(parentPropertyValue);
        project.setCascadingProject(parent);
        //If parent value equals to current value - isOverridden flag should be set to false.
        property.setOverridden(true);
        assertTrue(property.isOverridden());
        assertFalse(property.allowOverrideValue(parentProperty.getOriginalValue(), property.getValue()));
        property.onCascadingProjectChanged();
        assertFalse(property.isOverridden());
    }
}
