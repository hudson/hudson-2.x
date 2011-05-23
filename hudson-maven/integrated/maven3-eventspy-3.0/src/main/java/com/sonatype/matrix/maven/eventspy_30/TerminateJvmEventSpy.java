/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

import javax.inject.Named;

/**
 * Spy which simply terminates the JVM.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
public class TerminateJvmEventSpy
    extends EventSpySupport
{
    @Override
    public void init(final Context context) throws Exception {
        log.error("Terminating JVM");
        Runtime.getRuntime().exit(99);
    }
}