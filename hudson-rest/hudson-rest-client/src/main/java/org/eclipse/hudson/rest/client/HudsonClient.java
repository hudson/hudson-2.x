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

package org.eclipse.hudson.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;

/**
 * Hudson client.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface HudsonClient
{
    UUID getId();

    String getVersion();

    void open(URI uri);

    void open(URI uri, OpenOptions options);

    OpenOptions getOptions();
    
    boolean isOpen();

    void ensureOpened();

    void close();

    URI getBaseUri();

    Client getClient();

    UriBuilder uri();

    WebResource.Builder resource(URI uri);

    WebResource.Builder resource(UriBuilder uri);

    interface Extension
    {
        void init(HudsonClient client);

        void open() throws Exception;
        
        void close() throws Exception;
    }

    <T extends Extension> T ext(Class<T> type);

    static class NotConnectedFailure
        extends HudsonClientException
    {
        public NotConnectedFailure() {}
    }
}
