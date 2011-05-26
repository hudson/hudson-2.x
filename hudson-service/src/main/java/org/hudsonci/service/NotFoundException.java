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

package org.hudsonci.service;

/**
 * Thrown when a system entity that is needed to perform a service level action
 * cannot be found within the system.
 * <p>
 * An example of where this may be useful is when a source project identified by
 * a project name may not exist any more, and therefore cannot be copied to a
 * new project. Also, typically of the service APIs, all {@literal get*} methods
 * may throw this when an entity is not found.
 * <p>
 * Extend this class with a specific implementation for each type of system
 * entity that may not be found.
 * <p>
 * Service API users should do their best to verify an entity exists prior to
 * performing an operation that may trigger this exception.
 *
 * @since 2.1.0
 */
public abstract class NotFoundException extends ServiceRuntimeException {
    protected NotFoundException() {
    }

    protected NotFoundException(String message) {
        super(message);
    }

    protected NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    protected NotFoundException(Throwable cause) {
        super(cause);
    }
}
