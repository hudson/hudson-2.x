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

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thrown when an operation fails due to a missing document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DocumentNotFoundException
    extends DocumentException
{
    private final UUID id;

    public DocumentNotFoundException(final UUID id) {
        checkNotNull(id);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String getMessage() {
        return String.format("Document not found for ID: %s", getId());
    }
}
