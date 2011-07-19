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

package org.eclipse.hudson.maven.eventspy_30;

import javax.inject.Named;

/**
 * Spy which simply terminates the JVM.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
