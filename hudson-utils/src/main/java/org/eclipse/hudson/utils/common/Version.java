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

package org.eclipse.hudson.utils.common;

/**
 * Exposes version details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 *
 * @deprecated Prefer ProductInfo
 */
@Deprecated
public class Version
    extends VersionSupport
{
    private static Version instance;

    public static Version get() {
        if (instance == null) {
            instance = new Version();
        }
        return instance;
    }
}
