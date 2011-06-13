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

package org.hudsonci.rest.model.fault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds {@link FaultDTO} instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class FaultBuilder
{
    private static final Logger log = LoggerFactory.getLogger(FaultBuilder.class);

    public static FaultDTO build(final String type, final String message) {
        return new FaultDTO().withId(generateId()).withDetails(detail(type, message));
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    private static FaultDetailDTO detail(final String type, final String message) {
        checkNotNull(type);
        // message could be null
        return new FaultDetailDTO().withType(type).withMessage(message);
    }

    public static FaultDTO build(final Throwable cause) {
        checkNotNull(cause);

        log.warn("Building fault for: {}", cause.toString(), cause);

        FaultDTO fault = build(cause.getClass().getName(), cause.getMessage());

        // FIXME: This may be cause OOME in some situations (maybe?) so disable the nested details.
        //Throwable nested;
        //while ((nested = cause.getCause()) != null) {
        //    fault.getDetails().add(detail(nested.getClass().getName(), nested.getMessage()));
        //}

        return fault;
    }
}
