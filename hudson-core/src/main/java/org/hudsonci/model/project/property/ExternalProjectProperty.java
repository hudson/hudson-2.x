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

import org.hudsonci.api.model.ICascadingJob;

/**
 * Class property is intended to be used for ProjectProperties without correct equals and hashCode methods, such as
 * Builders, Publishers, etc.
 * <p/>
 * This property has additional {@link #modified} flag, that is used to define, whether current property was changed
 * from UI. If yes, cascading value will be updated. {@link #updateOriginalValue(Object, Object)} method for details.
 * <p/>
 * <p/>
 * Date: 10/20/11
 *
 * @author Nikita Levyankov
 */
public class ExternalProjectProperty<T> extends BaseProjectProperty<T> {

    private boolean modified;

    public ExternalProjectProperty(ICascadingJob job) {
        super(job);
    }

    /**
     * Method set modified state for current property.
     *
     * @param modified true if property was modified by user.
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * @return true if property was modified, false - otherwise.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * If property was not marked as {@link #isModified()} by calling {@link #setModified(boolean)} method with
     * true parameter value, than property will not be updated. This was implemented as the workaround for absent
     * equals methods for Publishers, BuildWrappers, etc.
     * <p/>
     * Such properties could be normally compared and use in cascading functionality.
     *
     * @param value new value to be set.
     * @param cascadingValue current cascading value.
     * @return true if value was updated, false - otherwise.
     */
    @Override
    protected boolean updateOriginalValue(T value, T cascadingValue) {
        return isModified() && super.updateOriginalValue(value, cascadingValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCascadingProjectSet() {
        setOverridden(isModified());
    }
}
