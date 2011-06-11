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

package org.hudsonci.gwt.common;

import com.google.gwt.user.client.ui.ListBox;

/**
 * Represents an {@link Enum} as a {@link ListBox}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class EnumListBox<T extends Enum<T>>
    extends ListBox
{
    private final Class<T> type;

    private T defaultValue;

    // FIXME: Need general way to deal with i18n of Enum.name() -> display text

    public EnumListBox(final Class<T> type) {
        assert type != null;
        this.type = type;

        T[] values = type.getEnumConstants();
        this.defaultValue = values[0];

        for (T value : values) {
            addItem(value.name());
        }

        setVisibleItemCount(1);
    }

    public EnumListBox(final Class<T> type, final T defaultValue) {
        this(type);
        setDefaultValue(defaultValue);
    }

    public Class<T> getType() {
        return type;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final T defaultValue) {
        assert defaultValue != null;
        this.defaultValue = defaultValue;
    }

    public void setSelected(final T value) {
        setSelectedIndex(value != null ? value.ordinal() : defaultValue.ordinal());
    }

    public T getSelected() {
        return Enum.valueOf(type, getValue(getSelectedIndex()));
    }
}
