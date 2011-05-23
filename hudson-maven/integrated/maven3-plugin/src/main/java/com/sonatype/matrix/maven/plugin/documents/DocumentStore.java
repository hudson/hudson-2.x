/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.plugin.documents.internal.DocumentStoreImpl;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * Provides backing for {@link DocumentDTO} persistence.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
