/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event;

import com.google.gwt.event.shared.EventHandler;
import com.sonatype.matrix.gwt.common.EventSupport;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.Document;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link Document}-based events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public abstract class DocumentEventSupport<H extends EventHandler>
    extends EventSupport<H>
{
    private final Document document;

    protected DocumentEventSupport(final Type<H> type, final Document document) {
        super(type);
        this.document = checkNotNull(document);
    }

    public Document getDocument() {
        return document;
    }
}
