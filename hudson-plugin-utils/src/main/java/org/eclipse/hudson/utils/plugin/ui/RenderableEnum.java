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

package org.eclipse.hudson.utils.plugin.ui;

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
