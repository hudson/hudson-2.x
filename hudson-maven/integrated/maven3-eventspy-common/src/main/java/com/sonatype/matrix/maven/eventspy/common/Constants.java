/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy.common;

import java.util.concurrent.TimeUnit;

/**
 * Holder for shared constants.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public interface Constants
{
    String PREFIX = "matrix.eventspy";

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

    String LOGGER_MATRIX_PROPERTY = LOGGER_PREFIX + ".matrix";
}