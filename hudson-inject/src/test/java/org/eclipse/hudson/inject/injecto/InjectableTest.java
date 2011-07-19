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

import org.eclipse.hudson.inject.SmoothieTestSupport;
import org.eclipse.hudson.inject.injecto.Injectable;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * Tests for {@link Injectable} types.
 */
public class InjectableTest
    extends SmoothieTestSupport
{
    @Test
    public void testInjectableWithImplements() {
        Thing thing = new InjectableThing();
        assertNotNull(thing);
        assertNotNull(thing.component);
    }

    public static class InjectableThing
        extends Thing
        implements Injectable
    {
        @Inject
        public void setComponent(SimpleComponent component) {
            super.setComponent(component);
        }
    }

    @Test
    public void testInjectableWithExtends() {
        Thing thing = new InjectableThing2();
        assertNotNull(thing);
        assertNotNull(thing.component);
    }

    public static class InjectableThing2
        extends InjectableThing
    {
    }

    @Test
    public void ensureInjectionWorksForInjectableSubInterface() {
        Thing thing = new SuperInjectableThing();
        assertNotNull(thing);
        assertNotNull(thing.component);
    }

    public static interface SuperInjectable
        extends Injectable
    {
    }

    public static class SuperInjectableThing
        extends Thing
        implements SuperInjectable
    {
        @Inject
        public void setComponent(SimpleComponent component) {
            super.setComponent(component);
        }
    }
}
