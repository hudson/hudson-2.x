/*******************************************************************************
 *
 * Copyright (c) 2010 Yahoo! Inc.
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

package hudson.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * @author dty
 */
@RunWith(Parameterized.class)
public class AutoCompleteSeederTest {

    public static class TestData {
        private String seed;
        private List<String> expected;

        public TestData(String seed, String... expected) {
            this.seed = seed;
            this.expected = Arrays.asList(expected);
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {new TestData("", "")},
            {new TestData("\"", "")},
            {new TestData("\"\"", "")},
            {new TestData("freebsd", "freebsd")},
            {new TestData(" freebsd", "freebsd")},
            {new TestData("freebsd ", "")},
            {new TestData("freebsd 6", "6")},
            {new TestData("\"freebsd", "freebsd")},
            {new TestData("\"freebsd ", "freebsd ")},
            {new TestData("\"freebsd\"", "")},
            {new TestData("\"freebsd\" ", "")},
            {new TestData("\"freebsd 6", "freebsd 6")},
            {new TestData("\"freebsd 6\"", "")},
        });
    }

    private String seed;
    private List<String> expected;

    public AutoCompleteSeederTest(TestData dataSet) {
        this.seed = dataSet.seed;
        this.expected = dataSet.expected;
    }

    @Test
    public void testAutoCompleteSeeds() throws Exception {
        AutoCompleteSeeder seeder = new AutoCompleteSeeder(seed);
        assertEquals(expected, seeder.getSeeds());

    }
}
