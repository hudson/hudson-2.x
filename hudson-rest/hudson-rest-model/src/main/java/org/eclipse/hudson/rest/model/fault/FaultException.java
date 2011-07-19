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

import java.util.Iterator;

import org.eclipse.hudson.rest.model.fault.FaultDTO;
import org.eclipse.hudson.rest.model.fault.FaultDetailDTO;

/**
 * {@link FaultDTO} exception container.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class FaultException
    extends RuntimeException
{
    // This is pointless, since Fault does not impl Serializable
    private static final long serialVersionUID = 1L;

    private final FaultDTO fault;

    public FaultException(final FaultDTO fault) {
        assert fault != null;
        this.fault = fault;
    }

    public FaultException(final String type, final String message) {
        this(FaultBuilder.build(type, message));
    }

    public FaultDTO getFault() {
        return fault;
    }

    @Override
    public String getMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append("Fault: ").append(fault.getId()).append("\n");

        Iterator<FaultDetailDTO> iter = fault.getDetails().iterator();
        while (iter.hasNext()) {
            FaultDetailDTO detail = iter.next();
            buff.append(String.format("[%s] %s", detail.getType(), detail.getMessage()));
            if (iter.hasNext()) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }
}
