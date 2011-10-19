/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Nikita Levyankov
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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hudsonci.api.model.IJob;
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
    private transient IJob job;
    private T originalValue;
    private boolean propertyOverridden;
    private boolean modified;

    /**
     * Instantiate new property.
     *
     * @param job owner of current property.
     */
    public BaseProjectProperty(IJob job) {
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
    public void setJob(IJob job) {
        if (null == job) {
            throw new IllegalArgumentException(INVALID_JOB_EXCEPTION);
        }
        this.job = job;
    }

    /**
     * @return job that property belongs to.
     */
    final IJob getJob() {
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
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T getCascadingValue() {
        if (null == propertyKey) {
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
        if (isOverridden() || null != originalValue) {
            return getOriginalValue();
        }
        return getCascadingValue();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void setValue(T value) {
        if (null == propertyKey) {
            throw new IllegalArgumentException(INVALID_PROPERTY_KEY_EXCEPTION);
        }
        value = prepareValue(value);
        if (!getJob().hasCascadingProject()) {
            setOriginalValue(value, false);
        } else {
            T cascadingValue = getCascadingValue();
            T candidateValue = null == value ? getDefaultValue() : value;
            if (isModified()) {
                if (allowOverrideValue(cascadingValue, candidateValue)) {
                    setOriginalValue(value, true);
                } else {
                    resetValue();
                }
            }
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
        setModified(overridden);
    }

    /**
     * {@inheritDoc}
     */
    public void resetValue() {
        this.originalValue = null;
        setOverridden(false);
        setModified(false);
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowOverrideValue(T cascadingValue, T candidateValue) {
        return ObjectUtils.notEqual(cascadingValue, candidateValue)
            && !EqualsBuilder.reflectionEquals(cascadingValue, candidateValue);
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
}
