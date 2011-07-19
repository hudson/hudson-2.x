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

package org.eclipse.hudson.inject.internal.extension;

import hudson.ExtensionComponent;

import java.util.List;

/**
 * Allows {@link hudson.PluginStrategy} complete control over how extensions are found.
 *
 * @since 1.397
 */
public interface ExtensionLocator
{
    <T> List<ExtensionComponent<T>> locate(Class<T> type);
}
