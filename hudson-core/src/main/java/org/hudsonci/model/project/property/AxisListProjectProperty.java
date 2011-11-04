/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak
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

import hudson.matrix.AxisList;
import hudson.util.DescribableList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hudsonci.api.model.IJob;

/**
 * Represents {@link hudson.matrix.AxisList} property.
 * <p/>
 * Date: 10/5/11
 *
 * @author Anton Kozak
 */
public class AxisListProjectProperty extends BaseProjectProperty<AxisList> {

    public AxisListProjectProperty(IJob job) {
        super(job);
    }

    @Override
    public AxisList getDefaultValue() {
        return new AxisList();
    }
    /**
     * {@inheritDoc}
     */
    public boolean allowOverrideValue(AxisList cascadingValue, AxisList candidateValue) {
        return ObjectUtils.notEqual(cascadingValue, candidateValue);
    }
}