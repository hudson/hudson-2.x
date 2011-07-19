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
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.BuildStatesDTO;
import org.eclipse.hudson.gwt.common.restygwt.ServiceFailureNotifier;
import org.eclipse.hudson.gwt.common.waitdialog.WaitPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildCoordinates;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildStateService;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link BuildInformationManager}.
 *
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class BuildInformationManagerImpl implements BuildInformationManager
{
    private final BuildStateService buildStateService;
    private final ArtifactDataProvider adp;
    private final EventBus eventBus;
    private final String projectName;
    private final int buildNumber;
    private final WaitPresenter waiter;
    private final ServiceFailureNotifier serviceFailureNotifier;

    @Inject
    public BuildInformationManagerImpl( final BuildCoordinates buildCoordinates, 
                                        final BuildStateService buildStateService, 
                                        final EventBus eventBus, 
                                        final ArtifactDataProvider adp,
                                        final WaitPresenter waiter, 
                                        final ServiceFailureNotifier serviceFailureNotifier)
    {
        checkNotNull(buildCoordinates);
        this.projectName = buildCoordinates.getProjectName();
        this.buildNumber = buildCoordinates.getBuildnumber();
        this.buildStateService = checkNotNull(buildStateService);
        this.eventBus = checkNotNull(eventBus);
        this.adp = checkNotNull(adp);
        this.waiter = checkNotNull(waiter);
        this.serviceFailureNotifier = checkNotNull(serviceFailureNotifier);
    }

    public void refresh()
    {
        waiter.startWaiting();
        buildStateService.getBuildStates( projectName, buildNumber, new MethodCallback<BuildStatesDTO>()
        {
            public void onSuccess( final Method method, final BuildStatesDTO response )
            {
                // TODO: can BuildStatesDTO be null?
                // If so, don't fire event; or pass empty list for safety.
                // null could mean that the build is in progress and just started
                // or somehow a build was navigated to (via direct url) that
                // didn't have any maven builds associated with it.
                eventBus.fireEvent( new BuildStateLoadedEvent( response ) );
                waiter.stopWaiting();
            }

            public void onFailure( final Method method, final Throwable exception )
            {
                serviceFailureNotifier.displayFailure("Failed to fetch build information.", method, exception);
            }
        } );
    }
    
    /**
     * Restructure to handle async requests for if/when this is used in a 
     * separate service request.
     * 
     * Something like publishing an Event on the EventBus that there's now 
     * artifact data for a module.
     */
    public Collection<ArtifactDTO> getConsumedArtifacts( final String moduleId )
    {
        Set<ArtifactDTO> consumedArtifacts = new HashSet<ArtifactDTO>();
        
        for ( ArtifactDTO artifact : adp.getList() )
        {
            for ( String dependendentModule : artifact.getDependentProjects() )
            {
                if( moduleId.equals( dependendentModule ) )
                {
                    // TODO: should the operations be filtered to only what's applicable
                    // to this moduleId?
                    // This would require filtering in the Display or creating 
                    // a new list here (increasing the client size).
                    // Maybe this would be a case for restructuring the build state
                    // so that each project contains it's artifacts and operations.
                    // Would probably still leave the artifact state as is when 
                    // used in eventspy to keep it more compact and less chatty, 
                    // but transform it once it's been committed to Hudson.
                    
                    consumedArtifacts.add( artifact );
                }
            }
        }
        
        return consumedArtifacts;
    }
    
    public Collection<ArtifactDTO> getProducedArtifacts( final String moduleId )
    {
        Set<ArtifactDTO> producedArtifacts = new HashSet<ArtifactDTO>();
        
        for ( ArtifactDTO artifact : adp.getList() )
        {
            if( moduleId.equals( artifact.getCreatedProject() ) )
            {
                producedArtifacts.add( artifact );
            }
        }
        
        return producedArtifacts;
    }
}
