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

package org.hudsonci.utils.plugin.ui;

import java.io.Serializable;

/**
 * Helper to allow rendering of a more friendly display name for an enum.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class RenderableEnum<E extends Enum<E>>
    implements Comparable<E>, Serializable
{
    private final E value;

    public RenderableEnum(final E value) {
        assert value != null;
        this.value = value;
    }

    // These are all getXXX so that jelly can reference them with an explicit method call.
    // ie. ${enum.name} -> getName(), otherwise its gotta be ${enum.name()}.

    public String getDisplayName() {
        // TODO: Allow lookup of human/i18n name, look up resource bundle for enum type, then key off enum name
        return value.name();
    }

    public String getName() {
        return value.name();
    }

    public int getOrdinal() {
        return value.ordinal();
    }

    public boolean equals(final Object obj) {
        return value.equals(obj);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public int compareTo(final E obj) {
        return value.compareTo(obj);
    }

    @SuppressWarnings({"unchecked"})
    public static RenderableEnum[] forEnum(final Class<? extends Enum> source) {
        assert source != null;
        Enum[] values = source.getEnumConstants();
        RenderableEnum[] target = new RenderableEnum[values.length];
        for (int i=0; i<values.length; i++) {
            target[i] = new RenderableEnum(values[i]);
        }
        return target;
    }
}
