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

package org.eclipse.hudson.rest.client.ext.maven;

import org.eclipse.hudson.rest.client.HudsonClient;

import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentsDTO;

/**
 * Client for {@link org.hudsonci.maven.plugin.documents.rest.DocumentResource}
 * @author plynch
 * @since 2.1.0
 */
public interface DocumentClient extends HudsonClient.Extension{
    DocumentDTO getDocument(final String uuid, final boolean summary);
    DocumentsDTO getDocuments(final boolean summary);
    DocumentDTO addDocument(final DocumentDTO document);
    DocumentDTO updateDocument(final String uuid, final DocumentDTO document);
    void removeDocument(final String uuid);
}
