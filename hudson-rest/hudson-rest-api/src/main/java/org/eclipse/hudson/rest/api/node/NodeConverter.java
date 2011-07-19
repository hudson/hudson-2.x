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

package org.eclipse.hudson.rest.api.node;


import org.eclipse.hudson.rest.api.internal.ConverterSupport;
import org.eclipse.hudson.rest.model.NodeDTO;
import org.eclipse.hudson.rest.model.NodeModeDTO;

/**
 * Converts {@link hudson.model.Node} to {@link NodeDTO} objects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class NodeConverter
    extends ConverterSupport
{
    public NodeDTO convert(final hudson.model.Node source) {
        assert source != null;

        log.trace("Converting: {}", source);

        NodeDTO target = new NodeDTO();
        target.setName(source.getNodeName());
        target.setDescription(source.getNodeDescription());
        target.setExecutors(source.getNumExecutors());
        target.setMode(convert(source.getMode()));
//        target.setConnected();
//        target.setOnline();
//        target.setOfflineCause();
//        target.setConnectTime();

        return target;
    }

    public NodeModeDTO convert(final hudson.model.Node.Mode source) {
        assert source != null;

        log.trace("Converting: {}", source);
    
        return NodeModeDTO.valueOf(source.name().toUpperCase());
    }
}
