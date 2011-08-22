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

package org.eclipse.hudson.rest.api.queue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.service.QueueService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link hudson.model.Queue} resource.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/queue")
public class QueueResource extends ResourceSupport {
    private final QueueService queueService;

    @Inject
    public QueueResource(final QueueService queueService) {
        this.queueService = checkNotNull(queueService);
    }

    @GET
    @Path("clear")
    public Response clear() {
        log.debug("Clearing the queue");
        queueService.getQueue().clear();
        return Response.noContent().build();
    }
}
