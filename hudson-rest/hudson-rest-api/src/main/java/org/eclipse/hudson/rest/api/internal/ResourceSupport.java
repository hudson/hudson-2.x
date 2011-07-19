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

package org.eclipse.hudson.rest.api.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.*;

/**
 * Base REST Resource
 *
 * @author plynch
 * @since 2.1.0
 */
@Produces({APPLICATION_JSON, APPLICATION_XML})
public abstract class ResourceSupport
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    // FIXME: Make this go away
}
