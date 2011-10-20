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

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProjectMock;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.NullSCM;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import hudson.tasks.JavadocArchiver;
import hudson.tasks.LogRotator;
import hudson.tasks.Shell;
import hudson.util.DescribableList;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
    public void testBaseProjectConstructor() {
        try {
            new BaseProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNotNull(property.getJob());
        assertEquals(project, property.getJob());
    }

    @Test
    public void testStringProjectConstructor() {
        try {
            new StringProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void testIntegerProjectConstructor() {
        try {
            new IntegerProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void testBooleanProjectPropertyConstructor() {
        try {
            new BooleanProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void testLogRotatorProjectPropertyConstructor() {
        try {
            new LogRotatorProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }
    @Test
    public void testDescribableListProjectPropertyConstructor() {
        try {
            new DescribableListProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }
    
    @Test
    public void testAxisListProjectPropertyConstructor() {
        try {
            new AxisListProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    @Test
    public void testSCMProjectPropertyConstructor() {
        try {
            new SCMProjectProperty(null);
            fail("Null should be handled by ProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Checks prepareValue method.
     */
    @Test
    public void testBaseProjectPropertyPrepareValue() {
        //BaseProject property doesn't perform any changes with value.
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNull(property.prepareValue(null));
        Object value = new Object();
        assertEquals(value, property.prepareValue(value));
    }

    @Test
    public void testBooleanProjectPropertyPrepareValue() {
        //Boolean property acts as BaseProperty
        BaseProjectProperty property = new BooleanProjectProperty(project);
        assertNull(property.prepareValue(null));
        assertFalse((Boolean) property.prepareValue(false));
        assertTrue((Boolean) property.prepareValue(true));
    }

    @Test
    public void testLogRotatorProjectPropertyPrepareValue() {
        //Boolean property acts as BaseProperty
        BaseProjectProperty property = new LogRotatorProjectProperty(project);
        assertNull(property.prepareValue(null));
        LogRotator value = new LogRotator(1, 1, 1, 1);
        assertEquals(value, property.prepareValue(value));
    }

    @Test
    public void testIntegerProjectPropertyPrepareValue() {
        //Integer property acts as BaseProperty
        BaseProjectProperty property = new IntegerProjectProperty(project);
        assertNull(property.prepareValue(null));
        int intValue = 10;
        assertEquals(intValue, property.prepareValue(intValue));
    }

    @Test
    public void testStringProjectPropertyPrepareValue() {
        //String project property trims string values to null and uses StringUtils.trimToNull logic.
        BaseProjectProperty property = new StringProjectProperty(project);
        assertNull(property.prepareValue(null));
        assertNull(property.prepareValue(""));
        assertNull(property.prepareValue("     "));
        assertEquals("abc", property.prepareValue("abc"));
        assertEquals("abc", property.prepareValue("    abc    "));
    }

    @Test
    public void testAxisListProjectPropertyPrepareValue() {
        //Boolean property acts as BaseProperty
        BaseProjectProperty property = new AxisListProjectProperty(project);
        assertNull(property.prepareValue(null));
        AxisList value = new AxisList();
        assertEquals(value, property.prepareValue(value));
    }

    @Test
    public void testSCMProjectPropertyPrepareValue() {
        //Boolean property acts as BaseProperty
        BaseProjectProperty property = new SCMProjectProperty(project);
        assertNull(property.prepareValue(null));
        SCM value = new NullSCM();
        assertEquals(value, property.prepareValue(value));
    }

    /**
     * Verify getDefaultValue method.
     */
    @Test
    public void testBaseProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNull(property.getDefaultValue());
    }

    @Test
    public void testStringProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new StringProjectProperty(project);
        assertNull(property.getDefaultValue());
    }

    @Test
    public void testIntegerProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new IntegerProjectProperty(project);
        assertEquals(0, property.getDefaultValue());
    }

    @Test
    public void testBooleanProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new BooleanProjectProperty(project);
        assertFalse((Boolean) property.getDefaultValue());
    }

    @Test
    public void testLogRotatorProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new LogRotatorProjectProperty(project);
        assertNull(property.getDefaultValue());
    }

    @Test
    public void testAxisListProjectPropertyGetDefaultValue() {
        AxisListProjectProperty property = new AxisListProjectProperty(project);
        assertEquals(property.getDefaultValue().size(), 0);
    }   
    
    @Test
    public void testDescribableListProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new DescribableListProjectProperty(project);
        assertNotNull(property.getDefaultValue());
    }


    @Test
    public void testScmtProjectPropertyGetDefaultValue() {
        BaseProjectProperty property = new SCMProjectProperty(project);
        assertEquals(new NullSCM(), property.getDefaultValue());
    }
    /**
     * Verify allowOverrideValue method.
     */
    @Test
    public void testBaseProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertTrue(property.allowOverrideValue(new Object(), null));
        assertTrue(property.allowOverrideValue(null, new Object()));
        //Test properties that don't have correct equals methods
        assertFalse(property.allowOverrideValue(new JavadocArchiver("", false), new JavadocArchiver("", false)));
        assertTrue(property.allowOverrideValue(new JavadocArchiver("", true), new JavadocArchiver("", false)));
    }

    @Test
    public void testBooleanProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new BooleanProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertFalse(property.allowOverrideValue(false, false));
        assertFalse(property.allowOverrideValue(true, true));
        assertTrue(property.allowOverrideValue(true, false));
        assertTrue(property.allowOverrideValue(false, true));
    }

    @Test
    public void testIntegerProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new IntegerProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertFalse(property.allowOverrideValue(1, 1));
        assertTrue(property.allowOverrideValue(1, 0));
        assertTrue(property.allowOverrideValue(0, 1));
    }

    @Test
    public void testStringProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new StringProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertFalse(property.allowOverrideValue("", ""));
        assertFalse(property.allowOverrideValue("abc", "abc"));
        assertFalse(property.allowOverrideValue("abc", "ABC"));
        assertTrue(property.allowOverrideValue(null, "abc"));
        assertTrue(property.allowOverrideValue("abc", null));
        assertTrue(property.allowOverrideValue("abc", "abcd"));
    }

    @Test
    public void testLogRotatorProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new LogRotatorProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertTrue(property.allowOverrideValue(new LogRotator(1, 1, 1, 1), null));
        assertTrue(property.allowOverrideValue(null, new LogRotator(1, 1, 1, 1)));
        assertTrue(property.allowOverrideValue(new LogRotator(1, 1, 1, 2), new LogRotator(1, 1, 1, 1)));
    }

    @Test
    public void testDescribableListProjectPropertyAllowOverrideValue() throws IOException{
        BaseProjectProperty property = new DescribableListProjectProperty(project);
        //Don't need to override null values
        assertFalse(property.allowOverrideValue(null, null));
        //Allow override if cascading or candidate are null
        assertTrue(property.allowOverrideValue(new DescribableList(project), null));
        assertTrue(property.allowOverrideValue(null, new DescribableList(project)));
        //Don't need to override Describable lists which has same Describable#data values, even if owners are not equal.
        assertFalse(property.allowOverrideValue(new DescribableList(project), new DescribableList(project)));
        assertFalse(property.allowOverrideValue(new DescribableList(project), new DescribableList(parent)));
        DescribableList describableList1 = new DescribableList(project);
        DescribableList describableList2 = new DescribableList(project);
        describableList1.add(new Shell("echo 'test3'"));
        describableList1.add(new Shell("echo 'test2'"));
        describableList2.add(new Shell("echo 'test2'"));
        describableList2.add(new Shell("echo 'test3'"));
        assertFalse(property.allowOverrideValue(describableList1, describableList2));

        DescribableList describableList3 = new DescribableList(parent);
        describableList3.replaceBy(describableList2.toList());
        assertFalse(property.allowOverrideValue(describableList1, describableList3));

        describableList2.add(new Shell("echo 'test1'"));
        assertTrue(property.allowOverrideValue(describableList1, describableList2));
    }

    @Test
    public void testAxisListProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new AxisListProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertTrue(property.allowOverrideValue(new AxisList(), null));
        assertTrue(property.allowOverrideValue(null, new AxisList()));
        assertTrue(property.allowOverrideValue(new AxisList().add(new Axis("DB", "mysql")), new AxisList()));
    }

    @Test
    public void testSCMProjectPropertyAllowOverrideValue() {
        BaseProjectProperty property = new SCMProjectProperty(project);
        assertFalse(property.allowOverrideValue(null, null));
        assertTrue(property.allowOverrideValue(new NullSCM(), null));
        assertTrue(property.allowOverrideValue(null, new NullSCM()));
        assertTrue(property.allowOverrideValue(new NullSCM(), new FakeSCM()));
    }


    /**
     * Verify getCascadingValue method.
     */
    @Test
    public void testBaseProjectPropertyGetCascadingValue() {
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
    public void testBaseProjectPropertyGetOriginalValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        assertNull(property.getOriginalValue());
        Object value = new Object();
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
        property.setValue(null);
        assertNull(property.getOriginalValue());
    }

    @Test
    public void testIntegerProjectPropertyGetOriginalValue() {
        int value = 10;
        BaseProjectProperty property = new IntegerProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
    }

    @Test
    public void testStringProjectPropertyGetOriginalValue() {
        String value = "abs";
        BaseProjectProperty property = new StringProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
    }

    @Test
    public void testBooleanProjectPropertyGetOriginalValue() {
        boolean value = Boolean.TRUE;
        BaseProjectProperty property = new BooleanProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
        property.setValue(null);
        assertFalse((Boolean) property.getOriginalValue());
    }

    @Test
    public void testLogRotatorProjectPropertyGetOriginalValue() {
        LogRotator value = new LogRotator(1, 1, 1, 1);
        BaseProjectProperty property = new LogRotatorProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
    }

    @Test
    public void testAxisListProjectPropertyGetOriginalValue() {
        AxisList value = new AxisList();
        value.add(new Axis("DB", "mysql"));
        BaseProjectProperty property = new AxisListProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
    }

    @Test
    public void testScmProjectPropertyGetOriginalValue() {
        SCM value = new NullSCM();
        BaseProjectProperty property = new SCMProjectProperty(project);
        property.setKey(propertyKey);
        property.setValue(value);
        assertEquals(value, property.getOriginalValue());
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
        assertFalse(property.isOverridden());
        assertNull(property.getOriginalValue());

        Object value = 12345;
        property.setValue(value);
        //If project doesn't have cascading - default boolean value is used for propertyOverridden flag
        assertFalse(property.isOverridden());
        assertEquals(value, property.getOriginalValue());

        String parentValue = "equalValue";
        BaseProjectProperty parentProperty = new BaseProjectProperty(parent);
        parentProperty.setKey(propertyKey);
        parentProperty.setValue(parentValue);
        parent.putJobProperty(propertyKey, parentProperty);
        project.setCascadingProject(parent);

        //If value set to null, need to check whether default value is equals to cascading
        property.setModified(true);
        property.setValue(null);
        assertTrue(property.isOverridden());
        String overriddenValue = "newValue";
        property.setValue(overriddenValue);
        assertTrue(property.isOverridden());

        //Check whether current value is not null, after setting equal-to-cascading value current will be null
        assertNotNull(property.getOriginalValue());
        assertTrue(property.isOverridden());
        property.setValue(parentValue);
        //Reset current property to null
        assertNull(property.getOriginalValue());
        //Cascading value is equal to current - reset flag to false.
        assertFalse(property.isOverridden());
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
        assertFalse(property.isOverridden());
        assertEquals(property.getDefaultValue(), value);

        property.setOverridden(true);
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

        property.setOverridden(true);
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
        property.setOverridden(true);
        assertNotNull(property.getOriginalValue());
        assertTrue(property.isOverridden());
        property.resetValue();
        assertNull(property.getOriginalValue());
        assertFalse(property.isOverridden());
    }

    /**
     * Test setOverridden method.
     */
    @Test
    public void testSetPropertyOverridden() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        //By default property isOverridden flag is set to false.
        assertFalse(property.isOverridden());
        //Test if flag is configured as expected. Set true/false values and check whether they are set correctly.
        property.setOverridden(true);
        assertTrue(property.isOverridden());
        property.setOverridden(false);
        assertFalse(property.isOverridden());
    }

    /**
     * Test returnOriginalValue method for BaseProjectProperty.
     */
    @Test
    public void testBaseProjectPropertyReturnOriginalValue() {
        BaseProjectProperty property = new BaseProjectProperty(project);
        //False - If isOverridden flag is false and original value is null
        assertFalse(property.returnOriginalValue());
        property.setOverridden(true);
        //True - If isOverridden flag is true or original value is not null
        assertTrue(property.returnOriginalValue());
        property.setOriginalValue(new Object(), false);
        assertTrue(property.returnOriginalValue());
    }

    /**
     * Test returnOriginalValue method for DescribableListProjectProperty.
     */
    @Test
    public void testDescribableListProjectPropertyReturnOriginalValue() throws IOException{
        DescribableListProjectProperty property = new DescribableListProjectProperty(project);
        //False - If isOverridden flag is false and original value is null
        assertFalse(property.returnOriginalValue());

        //False - If isOverridden flag is false and Describable list is null or empty
        property.setOriginalValue(null, false);
        assertFalse(property.returnOriginalValue());
        property.setOriginalValue(new DescribableList(project), false);
        assertFalse(property.returnOriginalValue());

        //True - If isOverridden flag is true or original value is not null
        property.setOverridden(true);
        assertTrue(property.returnOriginalValue());

        DescribableList originalValue = new DescribableList(project, Arrays.asList(new Object()));
        property.setOriginalValue(originalValue, false);
        assertTrue(property.returnOriginalValue());
    }

    private class FakeSCM extends SCM {
        @Override
        public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher,
                                                       TaskListener listener)
            throws IOException, InterruptedException {
            return null;
        }

        @Override
        protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher,
                                                          FilePath workspace, TaskListener listener,
                                                          SCMRevisionState baseline)
            throws IOException, InterruptedException {
            return null;
        }

        @Override
        public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace,
                                BuildListener listener,
                                File changelogFile) throws IOException, InterruptedException {
            return false;
        }

        @Override
        public ChangeLogParser createChangeLogParser() {
            return null;
        }
    }

}
