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
package org.hudsonci.api.model;

import java.io.Serializable;

/**
 * Represents Properties for Job,
 * <p/>
 * Date: 9/22/11
 *
 * @author Nikita Levyankov
 */
public interface IProjectProperty<T> extends Serializable {

    /**
     * Sets key for given property.
     *
     * @param key key.
     */
    void setKey(String key);

    /**
     * @return property key.
     */
    String getKey();

    /**
     * Sets the job, which is owner of current property.
     *
     * @param job {@link ICascadingJob}
     */
    void setJob(ICascadingJob job);

    /**
     * Sets property value.
     * If property has cascading value and properties' {@link #allowOverrideValue(Object, Object)} method returns true,
     * than value will be set to current property.<br/>
     * If property doesn't have cascading value, than value will be set directly.
     *
     * @param value value to set.
     */
    void setValue(T value);

    /**
     * Returns original property value.
     *
     * @return T
     */
    T getOriginalValue();

    /**
     * Returns cascading value if any.
     *
     * @return string.
     */
    T getCascadingValue();

    /**
     * @return true if value inherited from cascading project, false - otherwise,
     */
    boolean isOverridden();

    /**
     * Returns property value. If originalValue is not null or value was overridden for this
     * property - call {@link #getOriginalValue()}, otherwise call {@link #getCascadingValue()}.
     *
     * @return string.
     */
    T getValue();

    /**
     * This value will be taken if both cascading project and current project don't have values. Null by default.
     *
     * @return value
     */
    T getDefaultValue();

    /**
     * Resets value for given job. Default implementation sets Null value and resets propertyOverridden flag to false.
     */
    void resetValue();

    /**
     * Returns true, if cascading value should be overridden by candidate value.
     *
     * @param cascadingValue value from cascading project if any.
     * @param candidateValue candidate value.
     * @return true if cascading value should be replaced by candidate value.
     */
    boolean allowOverrideValue(T cascadingValue, T candidateValue);

    /**
     * Sets the overridden flag.
     *
     * @param overridden true - mark property as overridden, false - otherwise.
     */
    void setOverridden(boolean overridden);

    /**
     * Method that is called while changing cascading parent. Update property internal states.l
     */
    void onCascadingProjectChanged();

}
