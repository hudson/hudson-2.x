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

package org.eclipse.hudson.maven.plugin.dependencymonitor.internal;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Collection helpers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class CollectionsHelper
{
    /**
     * Check to see if bag1 contains any elements from bag2.
     */
    public static boolean containsAny(final Collection bag1, final Collection bag2) {
        checkNotNull(bag1);
        checkNotNull(bag2);

        for (Object obj : bag2) {
            if (bag1.contains(obj)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check to see if bag1 contains the same elements from bag2.
     */
    public static boolean differs(final Collection bag1, final Collection bag2) {
        checkNotNull(bag1);
        checkNotNull(bag2);

        return bag1.size() != bag2.size() || !bag1.containsAll(bag2);
    }
}

