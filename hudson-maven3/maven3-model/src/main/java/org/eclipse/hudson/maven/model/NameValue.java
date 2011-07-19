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

package org.eclipse.hudson.maven.model;

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
