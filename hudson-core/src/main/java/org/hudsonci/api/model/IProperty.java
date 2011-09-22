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
package org.hudsonci.api.model;

import java.io.IOException;
import java.io.Serializable;

/**
 * Represents Properties for Job,
 * <p/>
 * Date: 9/22/11
 *
 * @author Nikita Levyankov
 */
public interface IProperty<T> extends Serializable {

    /**
     * Sets key for given property.
     *
     * @param key key.
     */
    void setKey(Enum key);

    /**
     * Sets the job, which is owner of current property.
     *
     * @param job {@link IJob}
     */
    void setJob(IJob job);

    /**
     * Sets property value.
     *
     * @param value value to set.
     * @throws IOException if any.
     */
    void setValue(T value) throws IOException;

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
     * @throws IOException if any.
     */
    T getCascadingValue() throws IOException;

    /**
     * @return true if value inherited from cascading project, false - otherwise,
     */
    boolean isPropertyOverridden();

    /**
     * Returns property value. If originalValue is not null or value was overridden for this
     * property - call {@link #getOriginalValue()}, otherwise call {@link #getCascadingValue()}.
     *
     * @return string.
     * @throws IOException if any.
     */
    T getValue() throws IOException;

}
