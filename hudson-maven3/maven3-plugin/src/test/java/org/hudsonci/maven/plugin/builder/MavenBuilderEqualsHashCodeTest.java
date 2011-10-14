/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak, Nikita Levyankov
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
package org.hudsonci.maven.plugin.builder;

import java.util.Arrays;
import java.util.Collection;
import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Verifies the equals and the hashcode of {@link MavenBuilder}.
 * <p/>
 * Date: 10/5/2011
 *
 * @author Anton Kozak
 */
@RunWith(Parameterized.class)
public class MavenBuilderEqualsHashCodeTest {
    private MavenBuilder builder1;
    private MavenBuilder builder2;
    private boolean expectedResult;

    public MavenBuilderEqualsHashCodeTest(MavenBuilder builder1, MavenBuilder builder2, boolean expectedResult) {
        this.builder1 = builder1;
        this.builder2 = builder2;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection generateData() {
        BuildConfigurationDTO configuration1 = new BuildConfigurationDTO();
        configuration1.setGoals("clean install");
        BuildConfigurationDTO configuration2 = new BuildConfigurationDTO();
        configuration2.setGoals("clean install");
        configuration2.setOffline(true);

        return Arrays.asList(new Object[][]{
            {new MavenBuilder(configuration1), null, false},
            {new MavenBuilder(configuration1), new MavenBuilder(new BuildConfigurationDTO()), false},
            {new MavenBuilder(configuration1), new MavenBuilder(configuration2), false},
            {new MavenBuilder(configuration1), new MavenBuilder(configuration1), true},
            {new MavenBuilder(configuration2), new MavenBuilder(configuration2), true}
        });

    }

    @Test
    public void testEquals() {
        assertEquals(expectedResult, builder1.equals(builder2));
    }

    @Test
    public void testHashCode() {
        assertEquals(expectedResult, builder1.hashCode() == (builder2 == null? 0: builder2).hashCode());
    }
}
