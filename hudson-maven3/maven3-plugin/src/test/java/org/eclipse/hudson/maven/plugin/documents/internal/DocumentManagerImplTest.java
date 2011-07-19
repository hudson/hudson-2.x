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

import org.eclipse.hudson.maven.plugin.documents.DocumentNotFoundException;
import org.eclipse.hudson.maven.plugin.documents.DocumentStore;
import org.eclipse.hudson.maven.plugin.documents.DuplicateDocumentException;
import org.eclipse.hudson.maven.plugin.documents.internal.DocumentManagerImpl;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.utils.common.Iso8601Date;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;
import hudson.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.Collection;

import static org.eclipse.hudson.maven.model.config.DocumentStandardAttributeDTO.CREATED;
import static org.eclipse.hudson.maven.model.config.DocumentStandardAttributeDTO.CREATED_BY;
import static org.eclipse.hudson.maven.model.config.DocumentStandardAttributeDTO.UPDATED;
import static org.eclipse.hudson.maven.model.config.DocumentStandardAttributeDTO.UPDATED_BY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DocumentManagerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentManagerImplTest
{
    private static final String DUMMY_USER_ID = "dummy";

    private static final String INVALID_ID = "invalid-id";

    @Mock
    private SecurityService security;

    @Mock
    private User user;

    @Mock
    private DocumentStore store;

    private DocumentManagerImpl manager;

    @Before
    public void init() {
        manager = new DocumentManagerImpl(store, security);
        when(user.getId()).thenReturn(DUMMY_USER_ID);
        when(security.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void testInitialState() {
        Collection<DocumentDTO> documents = manager.getDocuments(false);
        assertNotNull(documents);
        assertEquals(0, documents.size());
    }

    @Test(expected = NullPointerException.class)
    public void testGetDocumentsWithNullType() {
        manager.getDocuments(null, false);
    }

    @Test(expected = NullPointerException.class)
    public void testGetDocumentWithNullId() {
        manager.getDocument(null, false);
    }

    @Test(expected = NullPointerException.class)
    public void testAddWithNullDocument() {
        manager.addDocument(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateWithNullDocument() {
        manager.addDocument(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveWithNullId() {
        manager.removeDocument(null);
    }

    private DocumentDTO createDocument() {
        return new DocumentDTO()
            .withId(manager.randomId())
            .withType(DocumentTypeDTO.CUSTOM)
            .withName("Doc")
            .withDescription("Desc")
            .withContent("Content");
    }

    @Test
    public void testValidateValid() {
        DocumentDTO doc1 = createDocument();
        manager.validate(doc1);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateMissingId() {
        DocumentDTO doc1 = createDocument().withId(null);
        manager.validate(doc1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInvalidId() {
        DocumentDTO doc1 = createDocument().withId(INVALID_ID);
        manager.validate(doc1);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateMissingType() {
        DocumentDTO doc1 = createDocument().withType(null);
        manager.validate(doc1);
    }

    @Test
    public void testSummaryOf() {
        DocumentDTO doc1 = createDocument();
        DocumentDTO doc2 = manager.summaryOf(doc1);
        assertEquals(doc1.getId(), doc2.getId());
        assertEquals(doc1.getType(), doc2.getType());
        assertEquals(doc1.getName(), doc2.getName());
        assertEquals(doc1.getDescription(), doc2.getDescription());
        assertNull(doc2.getContent());
    }

    @Test
    public void testCopyOf() {
        DocumentDTO doc1 = createDocument();
        DocumentDTO doc2 = manager.copyOf(doc1);
        assertThat(doc1.getId(), equalTo(doc2.getId()));
        assertThat(doc1.getType(), equalTo(doc2.getType()));
        assertThat(doc1.getName(), equalTo(doc2.getName()));
        assertThat(doc1.getDescription(), equalTo(doc2.getDescription()));
        assertThat(doc1.getContent(), equalTo(doc2.getContent()));

        // Make sure equality works
        assertThat(doc1, equalTo(doc2));

        // Make sure different objects
        assertNotSame(doc1, doc2);
    }

    private void assertIsIso8601Date(final String value) {
        try {
            Iso8601Date.parse(value);
        }
        catch (ParseException e) {
            fail("Expected ISO 8601 formatted date");
        }
    }

    private void assertHasCreatedAttributes(final DocumentDTO document) {
        Object created = document.getAttribute(CREATED);
        assertNotNull(created);
        assertEquals(created.getClass(), String.class);
        assertIsIso8601Date((String) created);

        Object createdBy = document.getAttribute(CREATED_BY);
        assertNotNull(createdBy);
        assertEquals(createdBy.getClass(), String.class);
        assertEquals(DUMMY_USER_ID, createdBy);
    }


    private void assertHasUpdateAttributes(final DocumentDTO document) {
        Object created = document.getAttribute(UPDATED);
        assertNotNull(created);
        assertEquals(created.getClass(), String.class);
        assertIsIso8601Date((String) created);

        Object createdBy = document.getAttribute(UPDATED_BY);
        assertNotNull(createdBy);
        assertEquals(createdBy.getClass(), String.class);
        assertEquals(DUMMY_USER_ID, createdBy);
    }

    @Test
    public void testAddDocument() {
        DocumentDTO doc1 = createDocument();
        DocumentDTO doc2 = manager.addDocument(doc1);
        assertNotNull(doc2);

        assertHasCreatedAttributes(doc2);
    }

    @Test
    public void testAddDocumentGenerateId() {
        DocumentDTO doc1 = createDocument().withId(null);
        DocumentDTO doc2 = manager.addDocument(doc1);
        assertNotNull(doc1.getId());
        assertNotNull(doc2.getId());
        assertEquals(doc1.getId(), doc2.getId());

        assertHasCreatedAttributes(doc2);
    }

    @Test(expected = DuplicateDocumentException.class)
    public void testAddDocumentDuplicate() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);
        manager.addDocument(doc1);
    }

    @Test
    public void testGetDocument() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);

        DocumentDTO doc2 = manager.getDocument(doc1.getId(), false);
        assertNotNull(doc2);
    }

    @Test
    public void testGetDocumentSummary() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);

        DocumentDTO doc2 = manager.getDocument(doc1.getId(), true);
        assertNotNull(doc2);
        assertNull(doc2.getContent());
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testGetDocumentMissing() {
        manager.getDocument(manager.randomId(), false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDocumentInvalidId() {
        manager.getDocument(INVALID_ID, false);
    }

    @Test
    public void testGetDocumentsInitial() {
        Collection<DocumentDTO> documents = manager.getDocuments(false);
        assertNotNull(documents);
        assertEquals(0, documents.size());
    }

    @Test
    public void testGetDocuments() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);
        DocumentDTO doc2 = createDocument();
        manager.addDocument(doc2);

        Collection<DocumentDTO> documents = manager.getDocuments(false);
        assertNotNull(documents);
        assertEquals(2, documents.size());

        // summary = false, should have content
        for (DocumentDTO doc : documents) {
            assertNotNull(doc.getContent());
        }
    }

    @Test
    public void testGetDocumentsSummary() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);
        DocumentDTO doc2 = createDocument();
        manager.addDocument(doc2);

        Collection<DocumentDTO> documents = manager.getDocuments(true);
        assertNotNull(documents);
        assertEquals(2, documents.size());

        // summary = true, should have content
        for (DocumentDTO doc : documents) {
            assertNull(doc.getContent());
        }
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testUpdateDocumentMissing() {
        DocumentDTO doc1 = createDocument();
        manager.updateDocument(doc1);
    }

    @Test
    public void testUpdateDocument() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);

        String name = "new name";
        doc1.setName(name);
        DocumentDTO doc2 = manager.updateDocument(doc1);
        assertNotNull(doc2);
        assertEquals(doc1.getId(), doc2.getId());
        assertHasUpdateAttributes(doc2);

        DocumentDTO doc3 = manager.getDocument(doc1.getId(), false);
        assertEquals(name, doc3.getName());
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testRemoveDocumentMissing() {
        manager.removeDocument(manager.randomId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDocumentInvalidId() {
        manager.removeDocument(INVALID_ID);
    }

    @Test
    public void testRemoveDocument() {
        DocumentDTO doc1 = createDocument();
        manager.addDocument(doc1);
        manager.removeDocument(doc1.getId());

        Collection<DocumentDTO> documents = manager.getDocuments(true);
        assertNotNull(documents);
        assertEquals(0, documents.size());
    }
}
