/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.eventspy.common;

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
