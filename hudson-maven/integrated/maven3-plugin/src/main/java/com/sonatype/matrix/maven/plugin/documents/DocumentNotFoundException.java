/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thrown when an operation fails due to a missing document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
