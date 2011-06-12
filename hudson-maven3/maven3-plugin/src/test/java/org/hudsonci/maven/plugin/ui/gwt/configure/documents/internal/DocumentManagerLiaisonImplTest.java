/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.junit.GWTMockUtilities;
import org.hudsonci.gwt.common.restygwt.internal.ServiceFailureNotifierImpl;
import org.hudsonci.gwt.common.waitdialog.WaitPresenter;
import org.hudsonci.gwt.icons.silk.SilkIcons;
import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.config.DocumentsDTO;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.Document;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentManagerLiaison.MessagesResource;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal.DocumentDataProvider;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal.DocumentManagerLiaisonImpl;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal.DocumentRestService;
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
