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

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.junit.GWTMockUtilities;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentsDTO;
import org.eclipse.hudson.gwt.common.restygwt.internal.ServiceFailureNotifierImpl;
import org.eclipse.hudson.gwt.common.waitdialog.WaitPresenter;
import org.eclipse.hudson.gwt.icons.silk.SilkIcons;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.Document;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentManagerLiaison.MessagesResource;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal.DocumentDataProvider;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal.DocumentManagerLiaisonImpl;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal.DocumentRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DocumentManagerLiaisonImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentManagerLiaisonImplTest
{
    @Mock
    private EventBus eventBus;

    @Mock
    private DocumentRestService documentRestService;

    @Mock
    private DocumentDataProvider documentDataProvider;

    @Mock
    private WaitPresenter waitWidget;

    @Mock
    private Method successMethod;

    @Mock
    private Method failureMethod;

    private DocumentManagerLiaisonImpl liaison;

    @Before
    public void init() {
        liaison = new DocumentManagerLiaisonImpl(
            eventBus,
            documentRestService,
            documentDataProvider,
            mock(SilkIcons.class),
            waitWidget,
            mock(ServiceFailureNotifierImpl.class),
            mock(MessagesResource.class)
        );

        // Setup RestyGWT success method/response
        Response successResponse = mock(Response.class);
        when(successResponse.getStatusText()).thenReturn(Status.OK.getReasonPhrase());
        when(successResponse.getStatusCode()).thenReturn(Status.OK.getStatusCode());
        when(successMethod.getResponse()).thenReturn(successResponse);

        // Setup RestyGWT failure method/response
        Response failureResponse = mock(Response.class);
        when(failureResponse.getStatusText()).thenReturn(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        when(failureResponse.getStatusCode()).thenReturn(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        when(failureMethod.getResponse()).thenReturn(failureResponse);
    }

    @BeforeClass
    public static void disableGwtCreate() {
        GWTMockUtilities.disarm();
    }

    @AfterClass
    public static void restoreGwtCreate() {
        GWTMockUtilities.restore();
    }

    @Test
    public void testCreate() {
        Document doc1 = liaison.create();
        assertNotNull(doc1);

        // Document should have been added to the data provider
        verify(documentDataProvider).add(doc1);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testFetchAllSuccess() {
        // Setup success response
        doAnswer(new Answer()
        {
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                MethodCallback<DocumentsDTO> callback = (MethodCallback<DocumentsDTO>) invocation.getArguments()[1];
                DocumentsDTO result = new DocumentsDTO().withDocuments(new DocumentDTO());
                callback.onSuccess(successMethod, result);
                return null;
            }
        }).when(documentRestService).getDocuments(anyBoolean(), any(MethodCallback.class));

        liaison.fetchAll();

        // Wait dialog should display and then hide
        InOrder dialogOrder = inOrder(waitWidget);
        dialogOrder.verify(waitWidget).startWaiting();
        dialogOrder.verify(waitWidget).stopWaiting();

        // Data is updated
        verify(documentDataProvider).set(anyCollectionOf(Document.class));
    }
}
