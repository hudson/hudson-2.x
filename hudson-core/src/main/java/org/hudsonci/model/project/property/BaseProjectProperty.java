/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
package org.hudsonci.model.project.property;

import hudson.util.DeepEquals;
import org.apache.commons.lang3.ObjectUtils;
import org.hudsonci.api.model.ICascadingJob;
import org.hudsonci.api.model.IProjectProperty;

/**
 * Base property implementation for project.
 * Contains common methods for setting and getting cascading and overridden properties.
 * <p/>
 * Date: 9/22/11
 *
 * @author Nikita Levyankov
 */
public class BaseProjectProperty<T> implements IProjectProperty<T> {
    static final String INVALID_JOB_EXCEPTION = "Project property should have not null job";
    static final String INVALID_PROPERTY_KEY_EXCEPTION = "Project property should have not null propertyKey";

    private transient String propertyKey;
    private transient ICascadingJob job;
    private T originalValue;
    private boolean propertyOverridden;

    /**
     * Instantiate new property.
     *
     * @param job owner of current property.
     */
    public BaseProjectProperty(ICascadingJob job) {
        setJob(job);
    }

    /**
     * {@inheritDoc}
     */
    public void setKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * {@inheritDoc}
     */
    public String getKey() {
        return propertyKey;
    }

    /**
     * {@inheritDoc}
     */
    public void setJob(ICascadingJob job) {
        if (null == job) {
            throw new IllegalArgumentException(INVALID_JOB_EXCEPTION);
        }
        this.job = job;
    }

    /**
     * @return job that property belongs to.
     */
    final ICascadingJob getJob() {
        return job;
    }

    /**
     * {@inheritDoc}
     */
    public void setOverridden(boolean overridden) {
        propertyOverridden = overridden;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T getCascadingValue() {
        if (null == getKey()) {
            throw new IllegalArgumentException(INVALID_PROPERTY_KEY_EXCEPTION);
        }
        return getJob().hasCascadingProject() ?
            (T) getJob().getCascadingProject().getProperty(propertyKey, this.getClass()).getValue() : getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOverridden() {
        return propertyOverridden;
    }

    /**
     * {@inheritDoc}
     */
    public T getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public T getValue() {
        if (returnOriginalValue()) {
            return getOriginalValue();
        }
        return getCascadingValue();
    }

    /**
     * Checks whether original or cascading value should be used.
     *
     * @return true to use original value, false - cascading value.
     */
    protected boolean returnOriginalValue() {
        return isOverridden() || null != originalValue;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void setValue(T value) {
        if (null == getKey()) {
            throw new IllegalArgumentException(INVALID_PROPERTY_KEY_EXCEPTION);
        }
        value = prepareValue(value);
        if (!getJob().hasCascadingProject()) {
            setOriginalValue(value, false);
        } else {
            updateOriginalValue(value, getCascadingValue());
        }
    }

    /**
     * Update value for cascading property. Before setting new value it will be checked for equality with cascading
     * value. If two values are equals - current value will be cleared via {@link #clearOriginalValue(Object)}
     * and will retrieved from parent. If not - value will be set directly.
     *
     * @param value new value to be set.
     * @param cascadingValue current cascading value.
     * @return true - if property was updated, false - otherwise if value was cleared.
     */
    protected boolean updateOriginalValue(T value, T cascadingValue) {
        T candidateValue = null == value ? getDefaultValue() : value;
        if (allowOverrideValue(cascadingValue, candidateValue)) {
            setOriginalValue(value, true);
            return true;
        } else {
            clearOriginalValue(value);
            return false;
        }
    }

    /**
     * Method that sets original value and mark it as overridden if needed. It was created to provide better flexibility
     * in subclasses.
     *
     * @param originalValue value to set
     * @param overridden true - to mark as overridden.
     */
    protected void setOriginalValue(T originalValue, boolean overridden) {
        this.originalValue = originalValue;
        setOverridden(overridden);
    }

    /**
     * Method that clears original value and marks it as overridden if needed.
     * Default implementation uses {@link #resetValue()}. Subclasses can override this method.
     *
     * @param originalValue value to set.
     */
    protected void clearOriginalValue(T originalValue) {
        resetValue();
    }

    /**
     * {@inheritDoc}
     */
    public void resetValue() {
        setOriginalValue(null, false);
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowOverrideValue(T cascadingValue, T candidateValue) {
        return ObjectUtils.notEqual(cascadingValue, candidateValue)
            && !DeepEquals.deepEquals(cascadingValue, candidateValue);
    }

    /**
     * Pre-process candidate value.
     *
     * @param candidateValue candidateValue.
     * @return candidateValue by default.
     */
    protected T prepareValue(T candidateValue) {
        return candidateValue;
    }

    /**
     * {@inheritDoc}
     */
    public T getOriginalValue() {
        return originalValue;
    }

    /**
     * {@inheritDoc}
     */
    public final void onCascadingProjectChanged() {
        if (getJob().hasCascadingProject()) {
            onCascadingProjectSet();
        } else {
            onCascadingProjectRemoved();
        }
    }

    /**
     * Executes when cascading parent is cleared. Default implementation marks property as not overridden.
     */
    protected void onCascadingProjectRemoved() {
        setOverridden(false);
    }

    /**
     * Executes when cascading project is set. Default implementation compares cascading and current value.
     * If values are not equal - mark property as overridden.
     */
    protected void onCascadingProjectSet() {
        setOverridden(allowOverrideValue(getCascadingValue(), getValue()));
    }
}
