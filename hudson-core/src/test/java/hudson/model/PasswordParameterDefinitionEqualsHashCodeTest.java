/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Nikita Levyankov
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
package hudson.model;

import hudson.util.Secret;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Equals and hashCode test for {@link PasswordParameterDefinition} object
 * <p/>
 * Date: 11/2/11
 *
 * @author Nikita Levyankov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class})
public class PasswordParameterDefinitionEqualsHashCodeTest {

    @Test
    @Ignore
    //TODO implement method
    public void testEquals() {
//        Secret secret = createMock(Secret.class);
//        expect(Secret.fromString(EasyMock.<String>anyObject())).andReturn(new Secret("value"))

//        expect(mailerDescriptor.getJsonSafeClassName()).andReturn(mailerKey);
        PasswordParameterDefinition o1 = new PasswordParameterDefinition("name1", "value", "description");
        assertEquals(o1, o1);
        assertFalse(o1.equals(null));
        assertFalse(o1.equals(new StringParameterDefinition("test1", "value", "description")));
        assertFalse(new PasswordParameterDefinition("name1", "value", null).equals(
            new PasswordParameterDefinition(null, "value", null)));
        assertFalse(new PasswordParameterDefinition(null, "value1", null).equals(
            new PasswordParameterDefinition(null, "value", null)));
        assertFalse(o1.equals(new PasswordParameterDefinition(null, "value1", null)));
        assertFalse(o1.equals(new PasswordParameterDefinition("name1", "value1", "description")));

        assertEquals(o1, new PasswordParameterDefinition("name1", "value", "description"));
        assertEquals(o1, new PasswordParameterDefinition("name1", "value", "description1"));
        assertEquals(new PasswordParameterDefinition(null, "value", "d1"),
            new PasswordParameterDefinition(null, "value", "d1"));
        assertEquals(new PasswordParameterDefinition(null, "value", null),
            new PasswordParameterDefinition(null, "value", null));
    }

    //TODO implement hashcode test
    public void testHashCode() {

    }
}
