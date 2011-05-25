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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.event.shared.EventBus;
import org.hudsonci.gwt.common.restygwt.ServiceFailureNotifier;
import org.hudsonci.gwt.common.waitdialog.WaitPresenter;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.BuildStatesDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildCoordinates;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildStateService;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ArtifactDataProvider;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.BuildInformationManagerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Interaction test of {@link BuildInformationManagerImpl}.
 * 
 * @author Jamie Whitehouse
 */
@RunWith( MockitoJUnitRunner.class )
public class BuildInformationManagerImplTest
{
    @Mock
    private BuildStateService service;
    
    @Mock
    private EventBus eventBus;

    @Mock // Unused, to satisfy null checks.
    private ArtifactDataProvider adp;

    @Mock
    private WaitPresenter waiter;
    
    @Mock
    private ServiceFailureNotifier failureNotifier;

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Test
    public void retrieveInfoSuccessFiresLoadedEvent()
    {
        BuildStatesDTO expectedBuildStates = createResponseData();
        answerBuildStatesRequest(createSuccessAnswer(null, expectedBuildStates));
        
        // Retrieve info.
        BuildInformationManager manager = new BuildInformationManagerImpl( 
            mock(BuildCoordinates.class), service, eventBus, adp, waiter, failureNotifier );
        manager.refresh();

        // Verify expectations.
        //verify( eventBus ).fireEvent( isA( BuildStateLoadedEvent.class ) );

        // Capture event for further expectations.
        ArgumentCaptor<BuildStateLoadedEvent> event = ArgumentCaptor.forClass( BuildStateLoadedEvent.class );
        verify( eventBus ).fireEvent( event.capture() );
        
        assertThat( event.getValue().getBuildStates(), hasSize( 1 ) );
        assertThat( event.getValue().getBuildStates(), contains( isIn( expectedBuildStates.getStates() ) ) );
    }
    
    @Test
    public void whenRefreshingUserIsNotifedToWait()
    {
        BuildInformationManagerImpl manager = new BuildInformationManagerImpl(
            mock(BuildCoordinates.class), service, eventBus, adp, waiter, failureNotifier);

        // Successful refresh
        answerBuildStatesRequest(createSuccessAnswer(null, null));
        manager.refresh();
        InOrder successOrder = inOrder( waiter );
        successOrder.verify( waiter ).startWaiting();
        successOrder.verify( waiter ).stopWaiting();
        
        // Failed refresh
        answerBuildStatesRequest(createFailureAnswer(null, null));
        manager.refresh();
        InOrder failureOrder = inOrder( waiter );
        failureOrder.verify( waiter ).startWaiting();
        failureOrder.verify( waiter ).stopWaiting();
    }
    
    @Test
    public void refreshFailureNotifiesUserOfError()
    {
        BuildInformationManagerImpl manager = new BuildInformationManagerImpl(
            mock(BuildCoordinates.class), service, eventBus, adp, waiter, failureNotifier);

        answerBuildStatesRequest(createFailureAnswer(null, null));
        manager.refresh();
        
        verify(failureNotifier).displayFailure(anyString(), any(Method.class), any(Throwable.class));
    }

    /**
     * A request answer that is successful.
     */
    @SuppressWarnings("unchecked")
    private Stubber createSuccessAnswer(final Method method, final BuildStatesDTO response) {
        Stubber buildStateRequestSuccess = doAnswer( new Answer()
        {
            public Object answer( InvocationOnMock invocation )
                throws Throwable
            {
                MethodCallback callback = (MethodCallback) invocation.getArguments()[2];
                callback.onSuccess( method, response );

                return null;
            }
        } );
        return buildStateRequestSuccess;
    }
    
    /**
     * A request answer that fails.
     */
    @SuppressWarnings("unchecked")
    private Stubber createFailureAnswer(final Method method, final Throwable response) {
        Stubber buildStateRequestSuccess = doAnswer( new Answer()
        {
            public Object answer( InvocationOnMock invocation )
                throws Throwable
            {
                MethodCallback callback = (MethodCallback) invocation.getArguments()[2];
                callback.onFailure( method, response );

                return null;
            }
        } );
        return buildStateRequestSuccess;
    }

    /**
     * Stubs the service with the specified answer.
     */
    @SuppressWarnings("unchecked")
    private void answerBuildStatesRequest(Stubber requestAnswer) {
        // Poor fluent API, read mockito docs for why.
        requestAnswer.when( service ).getBuildStates( anyString(), anyInt(), any( MethodCallback.class ) );
    }

    private BuildStatesDTO createResponseData()
    {
        return new BuildStatesDTO().withStates( new BuildStateDTO().withParticipatingProjects( new MavenProjectDTO().withName( "a test maven module" ) ) );
    }
    
    class InMemoryBuildCoordinates implements BuildCoordinates
    {
        private final String projectName;
        private final int buildnumber;

        public InMemoryBuildCoordinates( final String projectName, final int buildnumber )
        {
            this.projectName = projectName;
            this.buildnumber = buildnumber;
        }

        public String getProjectName()
        {
            return projectName;
        }

        public int getBuildnumber()
        {
            return buildnumber;
        }
    }
}
