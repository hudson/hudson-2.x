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

package org.hudsonci.maven.model;

/**
 * Container and parser for <tt>name=value</tt> bits.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class NameValue
{
    public static final String SEPARATOR = "=";

    public final String name;

    public final String value;

    private NameValue(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public static NameValue parse(final String input) {
        assert input != null;

        String name, value;

        int i = input.indexOf(SEPARATOR);
        if (i == -1) {
            name = input;
            value = Boolean.TRUE.toString();
        }
        else {
            name = input.substring(0, i);
            value = input.substring(i + 1, input.length());
        }

        return new NameValue(name.trim(), value);
    }

    public String toString() {
        return name + SEPARATOR + "'" + value + "'";
    }
}
