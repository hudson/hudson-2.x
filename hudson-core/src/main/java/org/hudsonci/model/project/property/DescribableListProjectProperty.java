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

import hudson.util.DescribableList;
import org.apache.commons.collections.CollectionUtils;
import org.hudsonci.api.model.IJob;

/**
 * Property represents DescribableList object.
 * <p/>
 * Date: 10/3/11
 *
 * @author Nikita Levyankov
 */
public class DescribableListProjectProperty extends BaseProjectProperty<DescribableList> {
    public DescribableListProjectProperty(IJob job) {
        super(job);
    }

    @Override
    public DescribableList getDefaultValue() {
        return new DescribableList(getJob());
    }

    @Override
    public boolean allowOverrideValue(DescribableList cascadingValue, DescribableList candidateValue) {
        return (null != candidateValue || null != cascadingValue)
            && ((null == cascadingValue || null == candidateValue)
            || !CollectionUtils.isEqualCollection(cascadingValue.toList(), candidateValue.toList()));
    }

    @Override
    protected boolean returnOriginalValue() {
        return isOverridden() || (null != getOriginalValue() && !getOriginalValue().isEmpty());
    }
}
