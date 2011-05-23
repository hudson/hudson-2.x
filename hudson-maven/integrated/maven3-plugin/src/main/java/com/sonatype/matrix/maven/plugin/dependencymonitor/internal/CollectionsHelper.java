/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor.internal;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Collection helpers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

