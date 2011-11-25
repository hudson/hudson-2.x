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
package org.hudsonci.model.project.property;

import hudson.util.CopyOnWriteList;
import org.hudsonci.api.model.ICascadingJob;

/**
 * Project property for {@link CopyOnWriteList}
 * <p/>
 * Date: 11/1/11
 *
 * @author Nikita Levyankov
 */
public class CopyOnWriteListProjectProperty extends BaseProjectProperty<CopyOnWriteList> {

    public CopyOnWriteListProjectProperty(ICascadingJob job) {
        super(job);
    }

    @Override
    public CopyOnWriteList getDefaultValue() {
        CopyOnWriteList result = new CopyOnWriteList();
        setOriginalValue(result, false);
        return result;
    }

    @Override
    protected boolean returnOriginalValue() {
        return isOverridden() || !getOriginalValue().isEmpty();
    }

    @Override
    public CopyOnWriteList getOriginalValue() {
        CopyOnWriteList result = super.getOriginalValue();
        return null != result ? result : getDefaultValue();
    }

    @Override
    protected void clearOriginalValue(CopyOnWriteList originalValue) {
        setOriginalValue(originalValue, false);
    }
}
