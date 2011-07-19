/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.slaves;

import hudson.model.Computer;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

import java.io.IOException;

/**
 * Partial implementation of {@link Computer} to be used in conjunction with
 * {@link AbstractCloudSlave}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.382
 */
public class AbstractCloudComputer<T extends AbstractCloudSlave> extends SlaveComputer {
    public AbstractCloudComputer(T slave) {
        super(slave);
    }

    @Override
    public T getNode() {
        return (T) super.getNode();
    }

    /**
     * When the slave is deleted, free the node.
     */
    @Override
    public HttpResponse doDoDelete() throws IOException {
        checkPermission(DELETE);
        try {
            getNode().terminate();
            return new HttpRedirect("..");
        } catch (InterruptedException e) {
            return HttpResponses.error(500,e);
        }
    }
}
