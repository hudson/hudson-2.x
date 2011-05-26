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
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.BuildStatesDTO;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildCoordinates;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildStateService;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent;

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
