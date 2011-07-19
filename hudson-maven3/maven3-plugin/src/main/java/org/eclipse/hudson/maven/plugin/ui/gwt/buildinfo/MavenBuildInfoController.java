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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.BuildStateLoadedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.BuildStateSelectedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.ModuleSelectedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ArtifactDataProvider;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls initial startup and extra configuration of the application.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class MavenBuildInfoController
{
    private final MainPanelPresenter mainPanel;
    private final BuildInformationManager manager;
    private final EventBus eventBus;
    private final ModuleDataProvider mdp;
    private final ArtifactDataProvider adp;
    private final BuildSummaryPresenter buildSummaryPresenter;
    private final ModuleInfoPresenter moduleInfoPresenter;
    private final ModuleInfoPickerPresenter moduleInfoPickerPresenter;

    @Inject
    public MavenBuildInfoController(final MainPanelPresenter mainPanel, 
                                    final BuildInformationManager buildInformationManager,
                                    final EventBus eventBus, 
                                    final ModuleDataProvider mdp, 
                                    final ArtifactDataProvider adp, 
                                    final BuildSummaryPresenter buildSummaryPresenter,
                                    final ModuleInfoPresenter moduleInfoPresenter, 
                                    final ModuleInfoPickerPresenter moduleInfoPickerPresenter) {
        this.mainPanel = mainPanel;
        this.manager = buildInformationManager;
        this.eventBus = eventBus;
        this.mdp = mdp;
        this.adp = adp;
        this.buildSummaryPresenter = checkNotNull(buildSummaryPresenter);
        this.moduleInfoPresenter = moduleInfoPresenter;
        this.moduleInfoPickerPresenter = moduleInfoPickerPresenter;
    }

    // TODO: inject with GIN then move the ModuleInfoPresenter to here.
    // OR put the EventBus handler config in something like an EventManager.
    public void start(HasWidgets container) {
        eventBus.addHandler(ModuleSelectedEvent.TYPE, new ModuleSelectedEvent.Handler()
        {
            public void onModulePicked(ModuleSelectedEvent event) {
                Log.debug("Module selection changed, updating view.");
                MavenProjectDTO module = event.getModule();

                // Module was deselected, clear the details.
                // TODO: should this logic be moved into the ModuleInfoPresenter.setModule
                // where it can detect nulls and perform the appropriate action?
                // Generally I don't like passing null objects around if I can help it but
                // it seems odd that this presenter like logic is external.
                if (module == null) {
                    moduleInfoPresenter.clear();
                }
                else {
                    moduleInfoPresenter.setModule(module);
                }
            }
        });

        eventBus.addHandler(BuildStateLoadedEvent.TYPE, new BuildStateLoadedEvent.Handler()
        {
            public void onLoad(BuildStateLoadedEvent event) {
                mainPanel.setBuildStates(event.getBuildStates());
            }
        });

        eventBus.addHandler(BuildStateSelectedEvent.TYPE, new BuildStateSelectedEvent.Handler()
        {
            public void onSelected(BuildStateSelectedEvent event) {
                buildStateSelected(event);
            }
        });

        mainPanel.bind(container);

        //addHistoryManagement();

        // Get the build info. This may be triggered by the initial history
        // if we start retrieving a subset of the data.
        manager.refresh();
    }

    public void buildStateSelected(BuildStateSelectedEvent event) {
        BuildStateDTO buildState = event.getBuildState();

        mdp.setList(buildState.getParticipatingProjects());
        adp.setList(buildState.getArtifacts());

        buildSummaryPresenter.setBuildState(buildState);
        moduleInfoPickerPresenter.refreshSelection();

        Log.debug("Refreshed data; mdp:" + mdp.getList().size() + " adp:" + adp.getList().size());
    }
    
    private void startHistoryManagement() {
        // Manage history change/navigation.
        // TODO: figure out how this fits in with Activities and Places.
        // TODO: probably pull this into a separate component.
        History.addValueChangeHandler(new ValueChangeHandler<String>()
        {
            public void onValueChange(ValueChangeEvent<String> event) {
                String historyToken = event.getValue();

                // Find the module matching the history token.
                if (historyToken.startsWith("module-")) {
                    String moduleId = historyToken.substring("module-".length());

                    for (MavenProjectDTO module : mdp.getList()) {
                        if (moduleId.equals(module.getId())) {
                            moduleInfoPickerPresenter.selectModule(module);
                            // Show the module info tab.
                            mainPanel.selectModuleInfo();
                            break;
                        }
                    }
                }
            }
        });

        // Navigate to initial history state (as determined by the URL).
        History.fireCurrentHistoryState();
    }
}
