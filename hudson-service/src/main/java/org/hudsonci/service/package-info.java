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

/**
 * The Service API provides modularity to the otherwise
 * large and growing functions exposed by {@link hudson.model.Hudson}
 * and the associated {@code hudson.model.*} hierarchy.
 *
 * <p>Security authorization is performed when appropriate to help API
 * users avoid needing to do this themselves. If additional security
 * checks are required outside of this, please use {@link SecurityService}.
 *
 * <p>Generally, unchecked exceptions are the norm in the service API, with all
 * thrown exceptions extending from {@link ServiceRuntimeException}.
 * Exception chaining is used so as to not lose the root cause of an exception.
 * The API is designed so that exceptions caused by external state
 * changes in the system can usually be avoided by using system state
 * accessing methods.
 *
 * <p> Generally the service API adopts the convention of methods beginning {@literal find*} will return
 * {@literal null} if the entity cannot be found. Single entity accessors beginning with {@literal get*}
 * will throw {@link NotFoundException}.
 *
 * @since 2.1.0
 */
package org.hudsonci.service;
