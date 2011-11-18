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

import antlr.ANTLRException;
import hudson.model.FreeStyleProjectMock;
import hudson.triggers.TimerTrigger;
import hudson.triggers.Trigger;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Contains test-cases for {@link TriggerProjectProperty}.
 * <p/>
 * Date: 11/17/11
 *
 * @author Nikita Levyankov
 */
public class TriggerProjectPropertyTest {

    private TriggerProjectProperty property;

    @Before
    public void setUp() {
        final String propertyKey = "propertyKey";
        FreeStyleProjectMock project = new FreeStyleProjectMock("project");
        property = new TriggerProjectProperty(project);
        property.setKey(propertyKey);
    }

    @Test
    public void testConstructor() {
        try {
            new TriggerProjectProperty(null);
            fail("Null should be handled by TriggerProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Verify {@link TriggerProjectProperty#clearOriginalValue(hudson.triggers.Trigger)} method.
     *
     * @throws antlr.ANTLRException if any
     */
    @Test
    public void testClearOriginalValue() throws ANTLRException {
        //Overridden flag should be cleared to false. Pre-set true value
        property.setOverridden(true);
        assertTrue(property.isOverridden());
        Trigger trigger = new TimerTrigger("* * * * *");
        property.clearOriginalValue(trigger);
        //Original value should be set with overridden flag == false
        assertFalse(property.isOverridden());
        assertTrue(trigger == property.getOriginalValue());
    }


    /**
     * Test updateOriginalValue method for TriggerProjectProperty.
     *
     * @throws antlr.ANTLRException if any
     */
    @Test
    public void testUpdateOriginalValue() throws ANTLRException {
        Trigger originalTrigger = new TimerTrigger("* * * * *");
        Trigger cascadingTrigger = new TimerTrigger("* * * * *");
        property.updateOriginalValue(originalTrigger, cascadingTrigger);
        //Property isn't overridden because of values equal.
        assertFalse(property.isOverridden());
        //If trigger property value equals to cascading be sure that sets original value instead of cascading.
        assertEquals(property.getOriginalValue(), originalTrigger);

        cascadingTrigger = new TimerTrigger("*/2 * * * *");
        property.updateOriginalValue(originalTrigger, cascadingTrigger);
        assertTrue(property.isOverridden());
        assertEquals(property.getOriginalValue(), originalTrigger);
    }

    /**
     * Verify {@link TriggerProjectProperty#onCascadingProjectRemoved()} method.
     *
     * @throws antlr.ANTLRException if any
     */
    @Test
    public void testOnCascadingProjectRemoved() throws ANTLRException {
        Trigger trigger = new TimerTrigger("* * * * *");
        property.setOriginalValue(trigger, false);
        assertTrue(trigger == property.getOriginalValue());
        assertFalse(property.isOverridden());
        property.onCascadingProjectRemoved();
        assertFalse(property.isOverridden());
        assertTrue(trigger == property.getOriginalValue());
    }

}
