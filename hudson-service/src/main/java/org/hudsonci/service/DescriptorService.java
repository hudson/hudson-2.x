/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.service;

import org.hudsonci.service.internal.DescriptorServiceImpl;

import com.google.inject.ImplementedBy;
import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;

/**
 * Service operations for {@link Descriptor} and {@link Describable}
 *
 * @since 2.1.0
 */
@ImplementedBy(DescriptorServiceImpl.class)
public interface DescriptorService {

    Descriptor getDescriptor(Class<? extends Describable> type);

    Descriptor getDescriptor(String className);

    <T extends Descriptor> T getDescriptorByType(Class<T> type);

    <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorList(Class<T> type);

    Descriptor getDescriptorOrDie(Class<? extends Describable> type);

}
