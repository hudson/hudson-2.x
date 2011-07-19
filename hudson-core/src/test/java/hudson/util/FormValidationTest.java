/*******************************************************************************
 *
 * Copyright (c) 2010, Seiji Sogabe
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     Seiji Sogabe
 *
 *******************************************************************************/ 

package hudson.util;

import junit.framework.TestCase;

/**
 * @author sogabe
 */
public class FormValidationTest extends TestCase {

    public void testValidateRequired_OK() {
        FormValidation actual = FormValidation.validateRequired("Name");
        assertEquals(FormValidation.ok(), actual);
    }

    public void testValidateRequired_Null() {
        FormValidation actual = FormValidation.validateRequired(null);
        assertNotNull(actual);
        assertEquals(FormValidation.Kind.ERROR, actual.kind);
    }

    public void testValidateRequired_Empty() {
        FormValidation actual = FormValidation.validateRequired("  ");
        assertNotNull(actual);
        assertEquals(FormValidation.Kind.ERROR, actual.kind);
    }

    // @Bug(7438)
    public void testMessage() {
        assertEquals("test msg", FormValidation.errorWithMarkup("test msg").getMessage());
    }
}
