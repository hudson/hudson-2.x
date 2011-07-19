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

package org.eclipse.hudson.maven.plugin.documents.internal;

import org.hudsonci.utils.test.TestUtil;
import org.eclipse.hudson.maven.plugin.documents.internal.DocumentStoreImpl;
import org.eclipse.hudson.service.SystemService;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link DocumentStoreImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentStoreImplTest
{
    private final TestUtil util = new TestUtil(this);

    @Mock
    private SystemService system;

    private DocumentStoreImpl store;

    @Before
    public void init() {
        store = new DocumentStoreImpl(system);
    }

    private File getRandomDir() {
        return util.resolveFile("target/tmp/documents-" + UUID.randomUUID());
    }

    @Test
    public void testDefaultConfiguration() {
        assertNotNull(store.getRootDir());
        assertNotNull(store.getMarshaller());
    }

    @Test
    public void testEmptyDirectoryNoDocuments() throws Exception {
        store.setRootDir(getRandomDir());
        Collection<DocumentDTO> documents = store.loadAll();
        assertNotNull(documents);
        assertEquals(0, documents.size());
    }

    @Test(expected = NullPointerException.class)
    public void testContainsNullId() {
        store.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void testLoadNullId() throws Exception {
        store.load(null);
    }

    @Test(expected = NullPointerException.class)
    public void testStoreNullDocument() throws Exception {
        store.store(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteNullDocument() throws Exception {
        store.delete(null);
    }

    @Test
    public void testDocumentLifecycle() throws Exception {
        File dir = getRandomDir();
        store.setRootDir(dir);

        UUID id = UUID.randomUUID();
        DocumentDTO doc1 = new DocumentDTO()
            .withId(id.toString())
            .withType(DocumentTypeDTO.CUSTOM)
            .withName("Doc")
            .withDescription("Desc")
            .withContent("Content");

        store.store(doc1);
        File file = store.fileFor(doc1);
        assertTrue(file.exists());
        assertTrue(store.contains(id));

        // Make sure it doesn't just say yes to anything
        assertFalse(store.contains(UUID.randomUUID()));

        // Make sure its rooted correctly and has a suffix
        assertEquals(dir, file.getParentFile());
        assertTrue(file.getName().endsWith(DocumentStoreImpl.FILE_SUFFIX));

        Collection<DocumentDTO> documents1 = store.loadAll();
        assertNotNull(documents1);
        assertEquals(1, documents1.size());

        assertTrue(store.contains(id));
        DocumentDTO doc2 = store.load(id);
        assertNotNull(doc2);
        assertEquals(doc2.getId(), id.toString());

        // Both documents should be equal
        assertEquals(doc1, doc2);

        store.delete(doc2);
        assertFalse(store.contains(id));
        assertFalse(file.exists());

        Collection<DocumentDTO> documents2 = store.loadAll();
        assertNotNull(documents2);
        assertEquals(0, documents2.size());
    }

    // TODO: Add corrupt file test

    // TODO: Add file with different suffix test

    // TODO: Add file with valid suffix & content but wrong name format
}
