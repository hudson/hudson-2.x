/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
