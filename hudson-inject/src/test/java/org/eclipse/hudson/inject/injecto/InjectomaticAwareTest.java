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

import com.google.inject.Key;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.SmoothieContainer;
import org.eclipse.hudson.inject.SmoothieTestSupport;
import org.eclipse.hudson.inject.injecto.Injectomatic;
import org.eclipse.hudson.inject.injecto.InjectomaticAware;
import org.junit.Test;

import javax.inject.Named;
import javax.inject.Singleton;

import static org.junit.Assert.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * Tests for {@link InjectomaticAware}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class InjectomaticAwareTest
    extends SmoothieTestSupport
{
    @Test
    public void testInjectoAwareWithImplements() {
        Injectomatic injecto = Smoothie.getContainer().get(Key.get(Injectomatic.class));
        assertNotNull(injecto);

        TestInjectomaticAware aware = Smoothie.getContainer().get(Key.get(TestInjectomaticAware.class));
        assertNotNull(aware.injecto);
        assertEquals(injecto, aware.injecto);
    }

    @Named
    @Singleton
    public static class TestInjectomaticAware
        implements InjectomaticAware
    {
        Injectomatic injecto;

        public void setInjectomatic(Injectomatic injecto) {
            this.injecto = injecto;
        }
    }

    @Test
    public void testInjectoAwareWithExtends() {
        SmoothieContainer container = Smoothie.getContainer();
        assertNotNull(container);

        Injectomatic injecto = container.get(Key.get(Injectomatic.class));
        assertNotNull(injecto);
        System.out.println(injecto);

        TestInjectomaticAware aware = container.get(Key.get(TestInjectomaticAware2.class));
        assertNotNull(aware.injecto);
        assertEquals(injecto, aware.injecto);
    }

    @Named
    @Singleton
    public static class TestInjectomaticAware2
        extends TestInjectomaticAware
    {
        public void setInjectomatic(Injectomatic injecto) {
            checkState(this.injecto == null);
            super.setInjectomatic(injecto);
        }
    }
}
