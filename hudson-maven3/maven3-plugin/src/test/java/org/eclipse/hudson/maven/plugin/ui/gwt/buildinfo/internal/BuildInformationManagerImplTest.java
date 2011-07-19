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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.event.shared.EventBus;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.BuildStatesDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import org.eclipse.hudson.gwt.common.restygwt.ServiceFailureNotifier;
import org.eclipse.hudson.gwt.common.waitdialog.WaitPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildCoordinates;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildStateService;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ArtifactDataProvider;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.BuildInformationManagerImpl;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
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
