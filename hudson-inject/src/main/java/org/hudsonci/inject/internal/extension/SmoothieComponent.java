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

package org.hudsonci.inject.internal.extension;

import hudson.Extension;
import hudson.ExtensionComponent;
import org.hudsonci.inject.Priority;
import org.sonatype.inject.BeanEntry;

import java.lang.annotation.Annotation;

/**
 * Smoothie component extension holder.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.396
 */
public class SmoothieComponent<T>
    extends ExtensionComponent<T>
{
    private final BeanEntry<Annotation,T> bean;

    private final T value;

    private final double priority;

    public SmoothieComponent(final BeanEntry<Annotation,T> bean) {
        super(null);
        this.bean = bean;
        this.value = bean.getValue();

        Double p = priorityOf(value);
        this.priority = p != null ? p : DEFAULT_PRIORITY;
    }

    public BeanEntry<Annotation,T> getBean() {
        return bean;
    }

    public Class<?> getType() {
        return getBean().getImplementationClass();
    }

    @Override
    public T getInstance() {
        return value;
    }

    public double getPriority() {
        return priority;
    }

    @Override
    public double ordinal() {
        return getPriority();
    }

    @Override
    public String toString() {
        return "SmoothieComponent{" +
            "type=" + getType() +
            ", priority=" + getPriority() +
            ", bean=" + bean +
            '}';
    }

    //
    // Priority helpers
    //

    public static final double DEFAULT_PRIORITY = 0;

    public static Double priorityOf(final Object component) {
        if (component == null) {
            return null;
        }
        return priorityOf(component.getClass());
    }

    public static Double priorityOf(final Class<?> type) {
        if (type != null) {
            Priority priority = type.getAnnotation(Priority.class);
            if (priority != null) {
                return priority.value();
            }
            Extension ext = type.getAnnotation(Extension.class);
            if (ext != null) {
                return ext.ordinal();
            }
        }
        return null;
    }

    public static Double priorityOf(final Annotation annotation) {
        if (annotation != null) {
            if (annotation instanceof Priority) {
                return ((Priority)annotation).value();
            }
            else if (annotation instanceof Extension) {
                return ((Extension)annotation).ordinal();
            }
        }
        return null;
    }
}
