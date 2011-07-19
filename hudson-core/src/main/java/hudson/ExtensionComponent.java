/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       Kohsuke Kawaguchi
 *
 *******************************************************************************/ 

package hudson;

/**
 * Discovered {@link Extension} object with a bit of metadata for Hudson.
 * This is a plain value object.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.356
 */
public class ExtensionComponent<T> implements Comparable<ExtensionComponent<T>> {
    private final T instance;
    private final double ordinal;

    public ExtensionComponent(T instance, double ordinal) {
        this.instance = instance;
        this.ordinal = ordinal;
    }

    public ExtensionComponent(T instance, Extension annotation) {
        this(instance,annotation.ordinal());
    }

    public ExtensionComponent(T instance) {
        this(instance,0);
    }

    /**
     * See {@link Extension#ordinal()}. Used to sort extensions.
     */
    public double ordinal() {
        return ordinal;
    }

    /**
     * The instance of the discovered extension.
     *
     * @return never null.
     */
    public T getInstance() {
        return instance;
    }

    /**
     * Sort {@link ExtensionComponent}s in the descending order of {@link #ordinal()}.
     */
    public int compareTo(ExtensionComponent<T> that) {
        double a = this.ordinal();
        double b = that.ordinal();
        if (a>b)    return -1;
        if (a<b)    return 1;
        return 0;
    }
}
