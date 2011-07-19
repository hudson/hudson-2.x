/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.gwt.common;

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
