/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy.common;

import java.io.Serializable;

/**
 * Container for access to document details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class DocumentReference
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String id;

    private final String content;

    public DocumentReference(final String id, final String content) {
        assert id != null;
        this.id = id;
        assert content != null;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return String.format("(managed-document-id:%s)", getId());
    }
}
