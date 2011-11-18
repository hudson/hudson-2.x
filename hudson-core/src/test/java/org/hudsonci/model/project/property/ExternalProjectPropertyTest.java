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
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Contains test-cases for {@link ExternalProjectProperty}.
 * <p/>
 * Date: 11/4/11
 *
 * @author Nikita Levyankov
 */
@SuppressWarnings("unchecked")
public class ExternalProjectPropertyTest {

    private ExternalProjectProperty property;
    private FreeStyleProjectMock project;

    @Before
    public void setUp() {
        project = new FreeStyleProjectMock("project");
        final String propertyKey = "propertyKey";
        property = new ExternalProjectProperty(project);
        property.setKey(propertyKey);
    }

    /**
     * Verify constructor
     */
    @Test
    public void testConstructor() {
        try {
            new ExternalProjectProperty(null);
            fail("Null should be handled by ExternalProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Verify {@link ExternalProjectProperty#updateOriginalValue(Object, Object)} method.
     */
    @Test
    public void testUpdateOriginalValue() {
        //If property is not modified, than it shouldn't be updated.
        assertFalse(property.isModified());
        assertFalse(property.updateOriginalValue(new Object(), new Object()));

        //If property is modified, than BaseProjectProperty#updateOriginalValue method will be called.
        //Result will be true, because in this case property value will be updated.
        property.setModified(true);
        assertTrue(property.isModified());
        assertFalse(property.updateOriginalValue(new Object(), new Object()));
        assertTrue(property.updateOriginalValue(new Object(), project));
    }

    /**
     * Verify {@link ExternalProjectProperty#onCascadingProjectSet()} method.
     */
    @Test
    public void testOnCascadingProjectSet() {
        assertFalse(property.isModified());
        assertFalse(property.isOverridden());
        //When cascading project was set, isModified should equal to isOverridden
        property.onCascadingProjectSet();
        assertEquals(property.isModified(), property.isOverridden());

        property.setModified(Boolean.TRUE);
        property.onCascadingProjectSet();
        assertTrue(property.isModified());
        assertEquals(property.isModified(), property.isOverridden());
    }
}
