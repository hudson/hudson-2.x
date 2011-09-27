/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Nikita Levyankov
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
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Contains test-cases for IProjectProperty and its' implementations.
 * <p/>
 * Date: 9/27/11
 *
 * @author Nikita Levyankov
 */
@SuppressWarnings("unchecked")
public class ProjectPropertyTest {
    private FreeStyleProjectMock project;
    private FreeStyleProjectMock parent;
    private final String propertyKey = "propertyKey";

    @Before
    public void setUp() {
        project = new FreeStyleProjectMock("project");
        parent = new FreeStyleProjectMock("parent");
    }

    /**
     * Verify all constructors for ProjectProperties hierarchy.
     */
    @Test
    public void testConstructor() {
        try {
            new BaseProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        try {
            new StringProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        try {
            new IntegerProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        try {
            new BooleanProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNotNull(property.getJob());
        assertEquals(project, property.getJob());
    }

    /**
     * Checks prepareValue method.
     */
    @Test
    public void testPrepareValue() {
        //BaseProject property doesn't perform any changes with value.
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNull(property.prepareValue(null));
        Object value = new Object();
        assertEquals(value, property.prepareValue(value));

        //Boolean property acts as BaseProperty
        property = new BooleanProjectProperty(project);
        assertNull(property.prepareValue(null));
        assertFalse((Boolean) property.prepareValue(false));
        assertTrue((Boolean) property.prepareValue(true));

        //Integer property acts as BaseProperty
        property = new IntegerProjectProperty(project);
        assertNull(property.prepareValue(null));
        int intValue = 10;
        assertEquals(intValue, property.prepareValue(intValue));

        //String project property trims string values to null and uses StringUtils.trimToNull logic.
        property = new StringProjectProperty(project);
        assertNull(property.prepareValue(null));
        assertNull(property.prepareValue(""));
        assertNull(property.prepareValue("     "));
        assertEquals("abc", property.prepareValue("abc"));
        assertEquals("abc", property.prepareValue("    abc    "));
    }

    /**
     * Verify getDefaultValue method.
     */
    @Test
    public void testGetDefaultValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNull(property.getDefaultValue());
        property = new StringProjectProperty(project);
        assertNull(property.getDefaultValue());
        property = new IntegerProjectProperty(project);
        assertEquals(0, property.getDefaultValue());
        property = new BooleanProjectProperty(project);
        assertFalse((Boolean) property.getDefaultValue());

    }

    /**
     * Verify allowOverrideValue method.
     */
    @Test
    public void testAllowOverrideValue() {
        //Test BaseProjectProperty.
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertTrue(property.allowOverrideValue(new Object(), null));
        assertTrue(property.allowOverrideValue(null, new Object()));

        //Test BooleanProjectProperty.
        property = new BooleanProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertFalse(property.allowOverrideValue(false, false));
        assertFalse(property.allowOverrideValue(true, true));
        assertTrue(property.allowOverrideValue(true, false));
        assertTrue(property.allowOverrideValue(false, true));

        //Test IntegerProjectProperty.
        property = new IntegerProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertFalse(property.allowOverrideValue(1, 1));
        assertTrue(property.allowOverrideValue(1, 0));
        assertTrue(property.allowOverrideValue(0, 1));

        //Test StringProjectProperty.
        property = new StringProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertFalse(property.allowOverrideValue("", ""));
        assertFalse(property.allowOverrideValue("abc", "abc"));
        assertFalse(property.allowOverrideValue("abc", "ABC"));
        assertTrue(property.allowOverrideValue(null, "abc"));
        assertTrue(property.allowOverrideValue("abc", null));
        assertTrue(property.allowOverrideValue("abc", "abcd"));
    }

    /**
     * Verify getCascadingValue method.
     */
    @Test
    public void testGetCascadingValue() {
        String parentValue = "parentValue";

        BaseProjectProperty property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        //If project doesn't have cascading project - default value is used as cascading value.
        assertEquals(property.getDefaultValue(), property.getCascadingValue());

        project.setCascadingProject(parent);
        property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        //If project has cascading project and cascading value is not set - default value is used.
        assertEquals(property.getDefaultValue(), property.getCascadingValue());

        BaseProjectProperty parentProperty = new BaseProjectProperty(parent);
        parentProperty.setKey(propertyKey);
        parentProperty.setValue(parentValue);
        parent.putJobProperty(propertyKey, parentProperty);
        project.setCascadingProject(parent);
        property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(parentValue);
        //If project has cascading project and cascading value is set - property value will be used.
        assertEquals(parentProperty.getOriginalValue(), property.getCascadingValue());
    }

    /**
     * Verify getOriginalValue method.
     */
    @Test
    public void testGetOriginalValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNull(property.getOriginalValue());
        Object value = new Object();

        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
        property.setValue(null);
        assertNull(property.getOriginalValue());

        value = 10;
        property = new IntegerProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());

        value = "abs";
        property = new StringProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());

        value = Boolean.TRUE;
        property = new BooleanProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
        property.setValue(null);
        assertFalse((Boolean) property.getOriginalValue());
    }

    /**
     * Verify setValue method.
     */
    @Test
    public void testSetValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(null);
        //If project doesn't have cascading - default boolean value is used for propertyOverridden flag
        assertFalse(property.isPropertyOverridden());
        assertNull(property.getOriginalValue());

        Object value = 12345;
        property.setValue(value);
        //If project doesn't have cascading - default boolean value is used for propertyOverridden flag
        assertFalse(property.isPropertyOverridden());
        assertEquals(value, property.getOriginalValue());

        String parentValue = "equalValue";
        BaseProjectProperty parentProperty = new BaseProjectProperty(parent);
        parentProperty.setKey(propertyKey);
        parentProperty.setValue(parentValue);
        parent.putJobProperty(propertyKey, parentProperty);
        project.setCascadingProject(parent);

        String overriddenValue = "newValue";
        property.setValue(overriddenValue);
        assertTrue(property.isPropertyOverridden());

        //Check whether current value is not null, after setting equal-to-cascading value current will be null
        assertNotNull(property.getOriginalValue());
        assertTrue(property.isPropertyOverridden());
        property.setValue(parentValue);
        //Reset current property to null
        assertNull(property.getOriginalValue());
        //Cascading value is equal to current - reset flag to false.
        assertFalse(property.isPropertyOverridden());
    }

    @Test
    public void testGetValue() {
        Integer propertyValue = 10;
        IntegerProjectProperty property = new IntegerProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(propertyValue);
        //if value is not null - return it
        assertEquals(propertyValue, property.getValue());
        property.setValue(null);
        assertNull(property.getOriginalValue());

        //if current value is set to null and property is not overridden - defaultValue (Zero) will be taken
        Integer value = property.getValue();
        assertNotNull(value);
        assertFalse(property.isPropertyOverridden());
        assertEquals(property.getDefaultValue(), value);

        property.setPropertyOverridden(true);
        //If property is overridden - return its actual value.
        assertNull(property.getValue());

        IntegerProjectProperty parentProperty = new IntegerProjectProperty(parent);
        parentProperty.setKey(propertyKey);
        parentProperty.setValue(propertyValue);
        parent.putJobProperty(propertyKey, parentProperty);

        project.setCascadingProject(parent);
        property = new IntegerProjectProperty(project);
        property.setKey(propertyKey);
        //if current value is null and is not overridden value, take from cascading
        assertNull(property.getOriginalValue());
        assertEquals(propertyValue, property.getValue());

        property.setPropertyOverridden(true);
        //Property is overridden - return current value even if it is null.
        assertNull(property.getOriginalValue());
        assertNull(property.getValue());
    }

    /**
     * Property should have not null property key.
     */
    @Test
    public void testNullPropertyKey() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        try {
            property.setValue("value");
            fail("PropertyKey shouldn't be null");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_PROPERTY_KEY_EXCEPTION, e.getMessage());
        }
        try {
            property.getCascadingValue();
            fail("PropertyKey shouldn't be null");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_PROPERTY_KEY_EXCEPTION, e.getMessage());
        }
        try {
            property.getValue();
            fail("PropertyKey shouldn't be null");
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
     * Resets project property value,
     */
    @Test
    public void testResetValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(new Object());
        property.setPropertyOverridden(true);
        assertNotNull(property.getOriginalValue());
        assertTrue(property.isPropertyOverridden());
        property.resetValue();
        assertNull(property.getOriginalValue());
        assertFalse(property.isPropertyOverridden());
    }
}
