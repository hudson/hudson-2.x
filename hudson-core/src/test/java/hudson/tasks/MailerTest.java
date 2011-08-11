/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Anton Kozak
 *
 *
 *******************************************************************************/
package hudson.tasks;

import hudson.model.Result;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.Bug;

import static junit.framework.Assert.assertEquals;

/**
 * Test for {@link Mailer}
 * </p>
 * Date: 8/11/2011
 *
 * @author Anton Kozak
 */
@RunWith(Parameterized.class)
public class MailerTest {

    private static Mailer publisher = new Mailer();

    private Result buildResult;
    private boolean expectedResult;

    public MailerTest(Result buildResult, boolean expectedResult) {
        this.buildResult = buildResult;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection dataProvider() {
        return Arrays.asList(new Object[][]{
            {Result.SUCCESS, true},
            {Result.ABORTED, true},
            {Result.FAILURE, true},
            {Result.NOT_BUILT, true},
            {Result.UNSTABLE, true}
        });
    }


    @Bug(4505)
    @Test
    public void testNeedsToRunAborted(){
        assertEquals(expectedResult, publisher.needsToRun(buildResult));
    }
}
