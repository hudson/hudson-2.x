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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ListDataProvider;

import javax.inject.Singleton;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.Document;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link AbstractDataProvider} for access to {@link Document} data.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
