/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.sonatype.matrix.maven.model.PropertiesDTO;
import com.sonatype.matrix.maven.model.PropertiesDTO.Entry;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.RuntimeEnvironmentDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.event.BuildStateSelectedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.gin.FirstShownInfoDisplay;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link MainPanelPresenter}.
 *
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Singleton
public class MainPanelPresenterImpl implements MainPanelPresenter
{
    private final MainPanelView view;
    private final EventBus eventBus;
    private List<BuildStateDTO> buildStates;
    private int currentBuildState = 0;
    private final BuildInformationManager dataManager;

    @Inject
    public MainPanelPresenterImpl( final MainPanelView view, @FirstShownInfoDisplay final IsWidget firstDisplay, final EventBus eventBus, final BuildInformationManager dataManager )
    {
        this.view = view;
        this.view.setFirstShownWidget( firstDisplay );
        this.view.setPresenter( this );
        this.eventBus = eventBus;
        this.dataManager = dataManager;
    }

    @Override
    public void bind( final HasWidgets container )
    {
        container.add( view.asWidget() );
    }

    @Override
    public void setBuildStates( final List<BuildStateDTO> buildStates )
    {
        this.buildStates = buildStates;
        
        buildStateSelected( currentBuildState );
        
        if( buildStates.size() > 1 )
        {
            List<String> stateNames = new ArrayList<String>();
            
            int i = 1;
            for ( BuildStateDTO state : buildStates )
            {
                stateNames.add( i + " : " +  state.getBuildConfiguration().getGoals() );
                i++;
            }
            
            view.setStateSelectionNames( stateNames );
            view.showStatePicker(currentBuildState);
        }
    }

    @Override
    public void buildStateSelected( final int selectedIndex )
    {
        currentBuildState = selectedIndex;
        BuildStateDTO selectedState = buildStates.get( selectedIndex );
        eventBus.fireEvent( new BuildStateSelectedEvent( selectedState ) );
        
        // As convenience, update this view to current build state. 
        updateViewData( selectedState );
    }

    @Override
    public void selectModuleInfo()
    {
        view.selectModuleInfo();
    }

    @Override
    public void refresh()
    {
        dataManager.refresh();
    }

    /**
     * TODO: the widgets needing this should be extracted to their own components
     * and embedded.  Allows for ease of change and isolation when we want to do 
     * lazy data fetching.
     */
    private void updateViewData( final BuildStateDTO selectedState )
    {
        // As convenience, update this view to current build state. 
        // Check for null objects since this info may not be available if there was 
        // a problem connecting to the Maven runner, or if it hasn't been started yet.
        // I think this is now confused since we changed the REST service to not send
        // null data as empty elements.  See JacksonCodec for the config.
        // While I don't like sending extra data, I also don't like null checks spread throughout the code.
        // TODO: is there any way to make nullsafe objects on the client side?
        RuntimeEnvironmentDTO runtime = selectedState.getRuntimeEnvironment();
        if( runtime != null )
        {
            Log.debug("Runtime data available, updating view.");
            view.setVersionData( maybeNullAsEmptyCollection( runtime.getVersionProperties() ) );
            view.setUserData( maybeNullAsEmptyCollection( runtime.getUserProperties() ) );
            view.setSystemData( maybeNullAsEmptyCollection( runtime.getSystemProperties() ) );
            view.setEnvironmentData( maybeNullAsEmptyCollection( runtime.getSystemEnvironment() ) );
        }
        else
        {
            Log.debug("Runtime data NOT available, updating view with empty collections.");
            view.setVersionData( Collections.<Entry>emptyList() );
            view.setUserData( Collections.<Entry>emptyList() );
            view.setSystemData( Collections.<Entry>emptyList() );
            view.setEnvironmentData( Collections.<Entry>emptyList() );
        }
    }
    
    private List<Entry> maybeNullAsEmptyCollection( PropertiesDTO propertiesDto )
    {
        return ( propertiesDto == null ? Collections.<Entry>emptyList() : propertiesDto.getEntries() );
    }
}
