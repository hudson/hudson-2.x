/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents.internal;

import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.model.config.DocumentTypeDTO;
import com.sonatype.matrix.service.SystemService;
import com.sonatype.matrix.testsupport.TestUtil;
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
