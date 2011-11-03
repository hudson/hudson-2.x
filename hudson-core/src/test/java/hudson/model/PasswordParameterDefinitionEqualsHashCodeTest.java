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
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;


/**
 * Equals and hashCode test for {@link PasswordParameterDefinition} object
 * <p/>
 * Date: 11/2/11
 *
 * @author Nikita Levyankov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class, Hudson.class})
public class PasswordParameterDefinitionEqualsHashCodeTest {

    @Before
    public void setUp() throws Exception {
        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        mockStatic(Secret.class);
        Secret secret = Whitebox.invokeConstructor(Secret.class, new Class<?>[]{String.class}, new String[]{"value"});
        expect(Secret.fromString(EasyMock.<String>anyObject())).andReturn(secret).anyTimes();
        expect(Secret.toString(EasyMock.<Secret>anyObject())).andReturn(secret.toString()).anyTimes();
        replayAll();
    }

    @Test
    public void testEquals() throws Exception {
        PasswordParameterDefinition o1 = new PasswordParameterDefinition("name1", "value", "description");
        assertEquals(o1, o1);
        assertFalse(o1.equals(null));
        assertFalse(o1.equals(new StringParameterDefinition("test1", "value", "description")));
        assertFalse(new PasswordParameterDefinition("name1", "value", null).equals(
            new PasswordParameterDefinition(null, "value", null)));

        assertEquals(o1, new PasswordParameterDefinition("name1", "value", "description"));
        assertEquals(o1, new PasswordParameterDefinition("name1", "value", "description1"));
        assertEquals(new PasswordParameterDefinition(null, "value", "d1"),
            new PasswordParameterDefinition(null, "value", "d1"));
        assertEquals(new PasswordParameterDefinition(null, "value", null),
            new PasswordParameterDefinition(null, "value", null));
        verifyAll();
    }

    @Test
    public void testHashCode() {
        PasswordParameterDefinition o1 = new PasswordParameterDefinition("name1", "value", "description");
        assertTrue(o1.hashCode() == o1.hashCode());
        assertFalse(o1.hashCode() == new StringParameterDefinition("test1", "value", "description").hashCode());
        assertFalse(new PasswordParameterDefinition("name1", "value", null).hashCode() ==
            new PasswordParameterDefinition(null, "value", null).hashCode());
        assertTrue(o1.hashCode() == new PasswordParameterDefinition("name1", "value", "description").hashCode());
        assertTrue(o1.hashCode() == new PasswordParameterDefinition("name1", "value", "description1").hashCode());
        assertTrue(new PasswordParameterDefinition(null, "value", "d1").hashCode() ==
            new PasswordParameterDefinition(null, "value", "d1").hashCode());
        assertTrue(new PasswordParameterDefinition(null, "value", null).hashCode() ==
            new PasswordParameterDefinition(null, "value", null).hashCode());
        verifyAll();
    }
}
