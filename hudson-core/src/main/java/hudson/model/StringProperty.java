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
package hudson.model;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.hudsonci.api.model.IJob;
import org.hudsonci.api.model.IProperty;

/**
 * String property.
 * <p/>
 * Date: 9/22/11
 *
 * @author Nikita Levyankov
 */
public class StringProperty implements IProperty<String> {

    private Enum propertyKey;
    private transient IJob job;
    private String originalValue;
    private boolean propertyOverridden;

    public void setKey(Enum propertyKey) {
        this.propertyKey = propertyKey;
    }

    public void setJob(IJob job) {
        this.job = job;
    }

    public StringProperty() {
    }

    public void setValue(String value) throws IOException {
        value = StringUtils.trimToNull(value);
        if (!job.hasCascadingProject()) {
            originalValue = value;
        } else if (!StringUtils.equalsIgnoreCase(
            (String) job.getCascadingProject().getProperty(propertyKey, this.getClass()).getValue(), value)) {
            originalValue = value;
            propertyOverridden = true;
        } else {
            this.originalValue = null;
            propertyOverridden = false;
        }
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getCascadingValue() throws IOException {
        return job.hasCascadingProject() ?
            (String) job.getCascadingProject().getProperty(propertyKey, this.getClass()).getValue() : null;
    }

    public boolean isPropertyOverridden() {
        return propertyOverridden;
    }

    public String getValue() throws IOException {
        if (isPropertyOverridden() || null != getOriginalValue()) {
            return getOriginalValue();
        }
        return getCascadingValue();
    }
}
