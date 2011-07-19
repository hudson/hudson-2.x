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

package org.eclipse.hudson.maven.plugin.documents;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.documents.internal.DocumentStoreImpl;
import org.eclipse.hudson.maven.model.config.DocumentDTO;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;


/**
 * Provides backing for {@link DocumentDTO} persistence.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(DocumentStoreImpl.class)
public interface DocumentStore
{
    boolean contains(UUID id);

    DocumentDTO load(UUID id) throws IOException;

    Collection<DocumentDTO> loadAll() throws IOException;

    void store(DocumentDTO document) throws IOException;

    void delete(DocumentDTO document) throws IOException;
}
