/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents;

/**
 * Thrown to indicate a failure with the document sub-system.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class DocumentException
    extends RuntimeException
{
    public DocumentException() {
        super();
    }

    public DocumentException(final String message) {
        super(message);
    }

    public DocumentException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DocumentException(final Throwable cause) {
        super(cause);
    }
}
