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

package org.eclipse.hudson.rest.model.fault;

import org.eclipse.hudson.rest.model.fault.FaultDTO;
import org.eclipse.hudson.rest.model.fault.FaultDetailDTO;
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
