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

package org.hudsonci.utils;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Some projects bundle Hamcrest into their jar which is very bad if it's not
 * shaded because it can interfere with the version of Hamcrest we want to use.
 * JUnit does this and is supposed to provide a 'junit-deps' artifact that does
 * not have these included.
 * 
 * Some projects have a dependency on a different version of Hamcrest than what
 * we want to use. This can be problem if our declared version isn't the nearest
 * match in resolving transitive dependencies. Mockito and JUnit-deps are examples
 * of this.
 * 
 * If a different version is used everything may seem fine until a test fails.
 * When it fails instead of the super helpful Hamcrest message an exception is
 * thrown:
 * java.lang.NoSuchMethodError: org.hamcrest.Matcher.describeMismatch(Ljava/lang/Object;Lorg/hamcrest/Description;)V
 * at org.hamcrest.MatcherAssert.assertThat(MatcherAssert.java:18)
 * 
 * The simple assertion in this test exposes these issues.
 * 
 * If it's success in our test-support module try copying it to your failing
 * module and running it. Chances are there's some library including junit or
 * a specific version of Hamcrest that doesn't match what we're expecting.
 * 
 * The Maven dependency tree can be useful in finding the offending artifact.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class HamcrestCompatibilityTest
{
    @Test
    @Ignore( "Intentionally failing test to show that Hamcrest is working properly" )
    public void checkHamcrestAssertThatWorks()
    {
        org.hamcrest.MatcherAssert.assertThat( "123", org.hamcrest.Matchers.equalToIgnoringCase( "1234" ) );
    }
}
