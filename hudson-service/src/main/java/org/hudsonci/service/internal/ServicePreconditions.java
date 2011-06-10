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

package org.hudsonci.service.internal;

import static com.google.common.base.Preconditions.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Preconditions to check common arguments to Service API calls
 *
 * @since 2.1.0
 */
public abstract class ServicePreconditions {

    protected static final Logger log = LoggerFactory.getLogger(ServicePreconditions.class);

    // should not normally instantiate
    protected ServicePreconditions() {
    }

    /**
     * Check a project buildNumber for being greater than zero.
     *
     * @param buildNumber buildNumber to test for validity
     * @return the passed in argument if valid
     * @throws IllegalArgumentException if buildNumber is less than one
     */
    public static int checkBuildNumber(final int buildNumber) {
        checkArgument(buildNumber > 0, "buildNumber (%s) must be greater than zero.", buildNumber);
        return buildNumber;
    }

    /**
     * Check a project build state index for shallow validity.
     *
     * @param index a value that should be non-negative
     * @return the passed in argument if valid
     * @throws IllegalArgumentException if index is negative
     */
    public static int checkBuildStateIndex(final int index) {
        checkArgument(index >= 0, "build state index (%s) must be greater than zero.", index);
        return index;
    }

    /**
     * Check a project builder index for shallow validity
     *
     * @param index a value that should be non-negative
     * @return the passed in argument if valid
     * @throws IllegalArgumentException if index is negative
     */
    public static int checkBuilderIndex(final int index) {
        checkArgument(index >= 0, "builder index (%s) must be greater than zero.", index);
        return index;
    }

    /**
     * Check the projectName of an {@link hudson.model.AbstractProject} for shallow validity
     *
     * @param projectName the project name to check
     * @return the unmodified projectName if not null
     * @throws NullPointerException if projectName is null
     */
    public static String checkProjectName(final String projectName) {
        return checkNotNull(projectName, "project name");
    }

    /**
     * Check the nodeName of an {@link hudson.model.Node} for shallow validity
     *
     * @param nodeName the node name to check
     * @return the unmodified nodeName if not null
     * @throws NullPointerException if nodeName is null
     */
    public static String checkNodeName(final String nodeName) {
        // FIXME: Probably need to validate encode/decode the nodeName
        return checkNotNull(nodeName, "node name");
    }

    /**
     * Check a {@link org.hudsonci.rest.model.DocumentDTO} ID (UUID) for
     * shallow validity
     *
     * @param id the Document id to check
     * @return the unmodified document id
     * @throws NullPointerException if document id is null
     * @throws IllegalArgumentException if document id is not in the expected
     * format of UUID
     */
    public static String checkDocumentId(final String id) {
        checkNotNull(id, "Document ID");
        checkUUID(id);
        return id;
    }

    /**
     * Check a uuid string for validity.
     *
     * @param uuid the argument to check for validity
     * @return the uuid argument unmodified
     * @throws IllegalArgumentException if uuid cannot be converted to a UUID
     * according to {@link java.util.UUID#fromString(String)}
     */
    public static String checkUUID(final String uuid) {
        UUID.fromString(uuid);
        return uuid;
    }

    /**
     * Check an argument for null. If it is null, then throw
     * NullPointerException
     *
     * @param reference a reference to what we are checking for null
     * @param msgKey a name describing what we are checking for null, to be
     * added to the NullPointerException msg in case of error
     */
    public static <T> T checkNotNull(T reference, String msgKey) {
        return Preconditions.checkNotNull(reference, "%s must not be null.", msgKey);
    }

    /**
     * IF the passed reference is null, throws a NullPointerException with
     * appropriate message.
     * <p>
     * While not itself a precondition, this is a common operation if a
     * precondition fails and can be used when custom validation was performed
     * for a rest argument.
     *
     * @param <T> reference to a value to check for null
     * @param reference reference to a value to check for null
     * @param clazz the type of reference, may be null
     * @return reference unchanged
     * @throws NullPointerException with message derived from clazz value if
     * reference is null
     */
    public static <T> T checkNotNull(T reference, Class<T> clazz) {
        if (reference == null) {
            final String msg = clazz == null ? "Required value" : clazz.getName() + " must not be null.";
            throw new NullPointerException(msg);
        }
        return reference;
    }
}
