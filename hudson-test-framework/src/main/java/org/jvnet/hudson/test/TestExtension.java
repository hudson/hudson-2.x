/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package org.jvnet.hudson.test;

import hudson.Extension;
import net.java.sezpoz.Indexable;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Works like {@link Extension} except used for inserting extensions during unit tests.
 *
 * <p>
 * This annotation must be used on a method/field of a test case class, or an nested type of the test case.
 * The extensions are activated only when the outer test class is being run.
 *
 * @author Kohsuke Kawaguchi
 * @see TestExtensionLoader
 */
@Indexable
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD})
@Documented
public @interface TestExtension {
    /**
     * To make this extension only active for one test case, specify the test method name.
     * Otherwise, leave it unspecified and it'll apply to all the test methods defined in the same class.
     *
     * <h2>Example</h2>
     * <pre>
     * class FooTest extends HudsonTestCase {
     *     public void test1() { ... }
     *     public void test2() { ... }
     *
     *     // this only kicks in during test1
     *     &#64;TestExtension("test1")
     *     class Foo extends ConsoleAnnotator { ... }
     *
     *     // this kicks in both for test1 and test2
     *     &#64;TestExtension
     *     class Bar extends ConsoleAnnotator { ... }
     * }
     * </pre>
     */
    String value() default "";
}
