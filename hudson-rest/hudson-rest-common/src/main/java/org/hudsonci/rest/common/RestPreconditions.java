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

package org.hudsonci.rest.common;

import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Preconditions to check common arguments to REST calls
 * 
 * @author plynch
 * @since 2.1.0
 */
public abstract class RestPreconditions {

    protected static final Logger log = LoggerFactory.getLogger(RestPreconditions.class);

    // should not normally instantiate
    protected RestPreconditions() {
    }

    /**
     * Check a project buildNumber for being greater than zero.
     * 
     * @param buildNumber
     *            buildNumber to test for validity
     * @return the passed in argument if valid
     * @throws WebApplicationException with {@link Response.Status#BAD_REQUEST} if buildNumber is less than one

     */
    public static int checkBuildNumber(final int buildNumber) {
        if (buildNumber < 1) {
            throwBadRequest("build number was " + buildNumber + " - must be greater than zero");
        }
        return buildNumber;
    }

    /**
     * Check a project build state index for shallow validity.
     * 
     * @param index
     *            a value that should be non-negative
     * @return the passed in argument if valid
     * @throws WebApplicationException with {@link Response.Status#BAD_REQUEST} if index is negative
     */
    public static int checkBuildStateIndex(final int index) {
        if (isNegative(index)) {
            throwBadRequest("build state index was " + index + " - must be zero or greater");
        }
        return index;
    }

    /**
     * Check a project builder index for shallow validity
     * 
     * @param index
     * @return the passed in argument if valid
     * @throws WebApplicationException with {@link Response.Status#BAD_REQUEST} if index is negative
     */
    public static int checkBuilderIndex(final int index) {
        if (isNegative(index)) {
            throwBadRequest("builder index was " + index + " - must be zero or greater");
        }
        return index;
    }

    /**
     * Check the projectName for shallow validity
     * 
     * @param projectName
     *            the project name to check
     * @return the unmodified projectName
     * @throws WebApplicationException
     *             status 400 if projectName is not a valid format or null
     */
    public static String checkProjectName(final String projectName) {
        if (projectName == null) {
            throwBadRequest("project name must not be null");
        }
        return projectName;
    }

    /**
     * Check a DocumentDTO ID for shallow validity
     * 
     * @param projectName
     *            the project name to check
     * @return the unmodified projectName
     * @throws WebApplicationException
     *             status 400 if document id is not a valid format or null
     */
    public static String checkDocumentId(final String id) {
        checkNotNull(id, "Document ID");
        checkUUID(id);
        return id;
    }

    /**
     * Check a uuid string for validity.
     * 
     * @param uuid
     * @throws WebApplicationException
     *             status 400 if UUID cannot be parsed from uuid string
     */
    protected static void checkUUID(final String uuid) {
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throwBadRequest("Document ID could not be parsed into a UUID");
        }
    }

    /**
     * Check an argument for null. If it is null, then calls
     * {@link #throwBadRequest(String)}.
     * 
     * @param reference
     * @param msgKey
     *            the key to lookup the value name
     */
    public static <T> T checkNotNull(T reference, String msgKey) {
        if (reference == null) {
            throwBadRequest(msgKey + " must not be null.");
        }
        return reference;
    }

    /**
     * IF the passed reference is null, throws a WebApplicationException with status 400 and generates a response
     * body containing suitable client message. The message will include the name of the class if clazz is null.
     * <p>
     * While not itself a precondition, this is a common operation if a
     * precondition fails and can be used when custom validation was performed
     * for a rest argument.
     *  
     * @param <T> reference to a value to check for null
     * @param reference reference to a value to check for null
     * @param clazz the type of reference, may be null
     * @return reference unchanged
     */
    public static <T> T checkNotNull(T reference, Class<T> clazz) {
        if (reference == null) {
            throwBadRequest(clazz == null ? "Required value" : clazz.getName() + " must not be null.");
        }
        return reference;
    }

    /**
     * Throws a WebApplicationException with status 400 and generates a response
     * body containing the specified message.
     * <p>
     * While not itself a precondition, this is a common operation if a
     * precondition fails and can be used when custom validation was performed
     * for a rest argument.
     * 
     * @param message
     *            the message to include in the response to the client
     * @throws WebApplicationException
     *             status 400 with message included as response entity
     */
    public static void throwBadRequest(final String message) {
        throwWebApplicationException(Response.Status.BAD_REQUEST, message);
    }
    
    /**
     * Throws a WebApplicationException with status 409 and generates a response
     * body containing the specified message.
     * <p>
     * While not itself a precondition, this is a common operation if a
     * precondition fails and can be used when custom validation was performed
     * for a rest argument.
     * 
     * @param message
     *            the message to include in the response to the client
     * @throws WebApplicationException
     *             status 400 with message included as response entity
     */
    public static void throwConflict(final String message) {
        throwWebApplicationException(Response.Status.CONFLICT, message);
    }

    /**
     * Throws a WebApplicationException with specified status and generates a
     * response body containing the specified message.
     * <p>
     * While not itself a precondition, this is a common operation if a
     * precondition fails and can be used when custom validation was performed
     * for a rest argument.
     * 
     * @param message
     *            the message to include in the response to the client
     * @param status
     *            the status code of the response in the exception
     * @throws WebApplicationException
     *             status 400 with message included as response entity
     */
    public static void throwWebApplicationException(final Response.Status status, final String message) {
        Preconditions.checkNotNull(status);
        log.info("precondition {} ({}) {}", new Object[] { status.getReasonPhrase(), status.getStatusCode(), message });
        Response resp = Response.status(status).entity(status.getFamily().toString() + ":" + message).build();
        throw new WebApplicationException(resp);
    }

    /**
     * DSL style negative checking
     * 
     * @param index
     * @return true if index is negative
     */
    protected static boolean isNegative(final int index) {
        return index < 0;
    }
}
