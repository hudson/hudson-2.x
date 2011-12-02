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
import hudson.util.DescribableList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.hudsonci.api.model.ICascadingJob;

/**
 * Property represents DescribableList object.
 * <p/>
 * Date: 10/3/11
 *
 * @author Nikita Levyankov
 */
public class DescribableListProjectProperty extends BaseProjectProperty<DescribableList> {
    public DescribableListProjectProperty(ICascadingJob job) {
        super(job);
    }

    @Override
    public DescribableList getDefaultValue() {
        DescribableList result = new DescribableList(getJob());
        setOriginalValue(result, false);
        return result;
    }

    @Override
    public boolean allowOverrideValue(DescribableList cascadingValue, DescribableList candidateValue) {
        if (null == cascadingValue && null == candidateValue) {
            return false;
        }
        if (null != cascadingValue && null != candidateValue) {
            List cascadingList = cascadingValue.toList();
            List candidateList = candidateValue.toList();
            return !(CollectionUtils.isEqualCollection(cascadingList, candidateList) || DeepEquals.deepEquals(cascadingList, candidateList));

        }
        return true;
    }

    @Override
    protected boolean returnOriginalValue() {
        return isOverridden() || !getOriginalValue().isEmpty();
    }

    @Override
    public DescribableList getOriginalValue() {
        DescribableList result = super.getOriginalValue();
        return null != result ? result : getDefaultValue();
    }
}
