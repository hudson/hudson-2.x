/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.inject;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.SmoothieContainer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link Smoothie}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class SmoothieTest
    extends SmoothieTestSupport
{
    @Test
    public void testGetContainer() {
        SmoothieContainer container = Smoothie.getContainer();
        assertNotNull(container);
        log.debug("Container: {}", container);
    }
}
