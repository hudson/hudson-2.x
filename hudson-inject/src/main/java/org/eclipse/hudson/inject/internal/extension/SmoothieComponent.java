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

package org.eclipse.hudson.inject.internal.extension;

import hudson.Extension;
import hudson.ExtensionComponent;

import org.eclipse.hudson.inject.Priority;
import org.sonatype.inject.BeanEntry;

import java.lang.annotation.Annotation;

/**
 * Smoothie component extension holder.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
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
        this.priority = priorityOf(bean);
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
    // IsOptional helpers
    //

    public static boolean isOptional(final BeanEntry<Annotation,?> beanEntry) {
        try {
            Boolean isOptional = isOptional(beanEntry.getKey());
            if (isOptional == null) {
                isOptional = isOptional(beanEntry.getImplementationClass());
                if (isOptional == null) {
                    isOptional = isOptional(beanEntry.getValue());
                    if (isOptional == null) {
                        return false;
                    }
                }
            }
            return isOptional.booleanValue();
        } catch (Throwable e) {
            return false;
        }
    }

    public static Boolean isOptional(final Object component) {
        if (component != null) {
            return isOptional(component.getClass());
        }
        return null;
    }

    public static Boolean isOptional(final Class<?> type) {
        if (type != null) {
            Extension extension = type.getAnnotation(Extension.class);
            if (extension != null) {
                return Boolean.valueOf(extension.optional());
            }
            ExtensionQualifier qualifier = type.getAnnotation(ExtensionQualifier.class);
            if (qualifier != null) {
                return Boolean.valueOf(qualifier.extension().optional());
            }
        }
        return null;
    }

    public static Boolean isOptional(final Annotation annotation) {
        if (annotation != null) {
            if (annotation instanceof Extension) {
                return Boolean.valueOf(((Extension)annotation).optional());
            }
            if (annotation instanceof ExtensionQualifier) {
                return Boolean.valueOf(((ExtensionQualifier)annotation).extension().optional());
            }
        }
        return null;
    }

    //
    // Priority helpers
    //

    public static final double DEFAULT_PRIORITY = 0;

    public static double priorityOf(final BeanEntry<Annotation, ?> beanEntry) {
        try {
            Double priority = priorityOf(beanEntry.getKey());
            if (priority == null) {
                priority = priorityOf(beanEntry.getImplementationClass());
                if (priority == null) {
                    priority = priorityOf(beanEntry.getValue());
                    if (priority == null) {
                        return DEFAULT_PRIORITY;
                    }
                }
            }
            return priority.doubleValue();
        } catch (Throwable e) {
            return DEFAULT_PRIORITY;
        }
    }

    public static Double priorityOf(final Object component) {
        if (component != null) {
            return priorityOf(component.getClass());
        }
        return null;
    }

    public static Double priorityOf(final Class<?> type) {
        if (type != null) {
            Extension extension = type.getAnnotation(Extension.class);
            if (extension != null) {
                return Double.valueOf(extension.ordinal());
            }
            ExtensionQualifier qualifier = type.getAnnotation(ExtensionQualifier.class);
            if (qualifier != null) {
                return Double.valueOf(qualifier.extension().ordinal());
            }
            Priority priority = type.getAnnotation(Priority.class);
            if (priority != null) {
                return Double.valueOf(priority.value());
            }
        }
        return null;
    }

    public static Double priorityOf(final Annotation annotation) {
        if (annotation != null) {
            if (annotation instanceof Extension) {
                return Double.valueOf(((Extension)annotation).ordinal());
            }
            if (annotation instanceof ExtensionQualifier) {
                return Double.valueOf(((ExtensionQualifier)annotation).extension().ordinal());
            }
            if (annotation instanceof Priority) {
                return Double.valueOf(((Priority)annotation).value());
            }
        }
        return null;
    }
}
