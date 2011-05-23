/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ListDataProvider;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.Document;

import javax.inject.Singleton;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link AbstractDataProvider} for access to {@link Document} data.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Singleton
public class DocumentDataProvider
    extends ListDataProvider<Document>
{
    public DocumentDataProvider() {
        super(Document.KEY_PROVIDER);
    }

    public void set(final Collection<Document> documents) {
        checkNotNull(documents);
        List<Document> list = getList();
        list.clear();
        list.addAll(documents);
    }

    public void add(final Document document) {
        checkNotNull(document);
        getList().add(document);
    }

    public void remove(final Document document) {
        checkNotNull(document);
        getList().remove(document);
    }
}
