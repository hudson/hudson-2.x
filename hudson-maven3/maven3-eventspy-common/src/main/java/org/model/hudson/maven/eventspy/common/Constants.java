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

package org.model.hudson.maven.eventspy.common;

import java.util.concurrent.TimeUnit;

/**
 * Holder for shared constants.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface Constants
{
    String PREFIX = "hudson.eventspy";

    String PORT_PROPERTY = PREFIX + ".port";

    String DELEGATE_PROPERTY = PREFIX + ".delegate";

    int CALLBACK_WAIT_TIMEOUT = 120;

    TimeUnit CALLBACK_WAIT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    String INVOKE_RECORD_FILE = PREFIX + ".invoke.record.file";

    // FIXME: Maybe drop these, probably not going to programmatically work with any of these.

    String LOGGING_PREFIX = PREFIX + ".logging";

    String LOGGING_CONSOLE_THRESHOLD_PROPERTY = LOGGING_PREFIX + ".console.threshold";

    String LOGGING_FILE_NAME_PROPERTY = LOGGING_PREFIX + ".file.name";

    String LOGGING_FILE_THRESHOLD_PROPERTY = LOGGING_PREFIX + ".file.threshold";

    String LOGGER_PREFIX = PREFIX + ".logger";

    String LOGGER_ROOT_PROPERTY = LOGGER_PREFIX + ".root";

    String LOGGER_MAVEN_PROPERTY = LOGGER_PREFIX + ".maven";
}
