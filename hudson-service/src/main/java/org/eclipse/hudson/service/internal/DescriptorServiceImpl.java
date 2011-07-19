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

package org.eclipse.hudson.service.internal;

import static org.eclipse.hudson.service.internal.ServicePreconditions.*;
import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.hudson.service.DescriptorService;

/**
 * Default implementation of {@link DescriptorService}
 *
 * <b>Note:</b> Should not normally access this publicly since no security
 * checks are present.
 *
 * @since 2.1.0
 */
@Named
@Singleton
public class DescriptorServiceImpl extends ServiceSupport implements DescriptorService {
    @Inject
    DescriptorServiceImpl() {
    }

    public Descriptor getDescriptor(final String className) {
        checkNotNull(className, "class name");
        return getHudson().getDescriptor(className);
    }

    public Descriptor getDescriptor(Class<? extends Describable> type) {
        checkNotNull(type, "type");
        return getHudson().getDescriptor(type);
    }

    public Descriptor getDescriptorOrDie(Class<? extends Describable> type) {
        checkNotNull(type, "type");
        return getHudson().getDescriptorOrDie(type);
    }

    public <T extends Descriptor> T getDescriptorByType(Class<T> type) {
        checkNotNull(type, "type");
        return getHudson().getDescriptorByType(type);
    }

    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorList(
            Class<T> type) {
        checkNotNull(type, "type");
        return getHudson().getDescriptorList(type);
    }

}
