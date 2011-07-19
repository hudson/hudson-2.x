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

package org.eclipse.hudson.rest.common;

import javax.ws.rs.core.MediaType;

/**
 * REST sub-system constants.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface Constants
{
    String HUDSON_HEADER = "X-Hudson";

    String BASE_REST_PATH = "rest";

    MediaType FAULT_v1_JSON_TYPE = new MediaType("application", "vnd.hudsonci.fault-v1+json");

    String FAULT_v1_JSON = "application/vnd.hudsonci.fault-v1+json";

    MediaType FAULT_v1_XML_TYPE = new MediaType("application", "vnd.hudsonci.fault-v1+xml");

    String FAULT_v1_XML = "application/vnd.hudsonci.fault-v1+xml";
}
