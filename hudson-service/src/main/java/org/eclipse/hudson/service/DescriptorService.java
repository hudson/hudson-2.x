/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.service;

import org.eclipse.hudson.service.internal.DescriptorServiceImpl;

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
