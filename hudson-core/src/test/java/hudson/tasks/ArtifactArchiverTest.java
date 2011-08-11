/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation,  Anton Kozak
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
 * Test for {@link ArtifactArchiver}
 * </p>
 * Date: 8/11/2011
 *
 * @author Anton Kozak
 */
@RunWith(Parameterized.class)
public class ArtifactArchiverTest {

    private static ArtifactArchiver publisher = new ArtifactArchiver(null, null, false, null, false);

    private Result buildResult;
    private boolean expectedResult;

    public ArtifactArchiverTest(Result buildResult, boolean expectedResult) {
        this.buildResult = buildResult;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection dataProvider() {
        return Arrays.asList(new Object[][]{
            {Result.SUCCESS, true},
            {Result.ABORTED, false},
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
