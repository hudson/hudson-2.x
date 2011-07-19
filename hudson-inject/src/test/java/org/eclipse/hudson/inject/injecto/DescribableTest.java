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

package org.eclipse.hudson.inject.injecto;

import hudson.model.Describable;
import hudson.model.Descriptor;

import org.eclipse.hudson.inject.SmoothieTestSupport;
import org.eclipse.hudson.inject.injecto.Injectable;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link Describable} types.
 */
public class DescribableTest
    extends SmoothieTestSupport
{
    @Test
    public void testDescribable() {
        Thing thing = new DescribableThing();
        assertNotNull(thing);
        assertNotNull(thing.component);
    }

    public static class DescribableThing
        extends Thing
        implements Describable<DescribableThing>
    {
        @Inject
        public void setComponent(SimpleComponent component) {
            super.setComponent(component);
        }

        public Descriptor<DescribableThing> getDescriptor() {
            return null;
        }
    }

    @Test
    public void testDescribableWithImplementsInjectable() {
        Thing thing = new DescribableThing2();
        assertNotNull(thing);
        assertNotNull(thing.component);
    }

    public static class DescribableThing2
        extends Thing
        implements Describable<DescribableThing>, Injectable
    {
        @Inject
        public void setComponent(SimpleComponent component) {
            super.setComponent(component);
        }

        public Descriptor<DescribableThing> getDescriptor() {
            return null;
        }
    }
}
