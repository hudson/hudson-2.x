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

import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter;

import java.util.Collections;

/**
 * Default implementation of {@link ModuleInfoPresenter}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ModuleInfoPresenterImpl
    implements ModuleInfoPresenter
{
    private final ModuleInfoView view;
    private final BuildInformationManager infoManager;

    @Inject
    public ModuleInfoPresenterImpl( final ModuleInfoView view, final BuildInformationManager infoManager )
    {
        this.view = view;
        this.infoManager = infoManager;
    }

    public void setModule( final MavenProjectDTO module )
    {
        // Summary Tab
        ModuleFormatter moduleFormatter = new ModuleFormatter( module );
        view.setBuildStatus( module.getBuildSummary().getResult() );
        view.setBuildSummary( formatSummary( module, moduleFormatter ) );
        view.setCoordinates( "Coordinates: " + module.getCoordinates() );
        view.setProfileSummary( "Active profiles: " + moduleFormatter.profiles( true ) );
        view.setProducedArtifacts( infoManager.getProducedArtifacts( module.getId() ) );

        // Artifact Tab
        view.setArtifactInfo( infoManager.getConsumedArtifacts( module.getId() ) );

        view.showInfo();
    }

    public void clear()
    {
        view.hideInfo();

        view.setBuildSummary( null );
        view.setProfileSummary( null );
        view.setProducedArtifacts( Collections.<ArtifactDTO>emptyList() );
        
        view.setArtifactInfo( Collections.<ArtifactDTO>emptyList() );
    }

    private String formatSummary( final MavenProjectDTO module, final ModuleFormatter moduleFormatter )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( module.getName() ).append( formatDuration( moduleFormatter ) );
        return sb.toString();
    }

    private String formatDuration( final ModuleFormatter moduleFormatter )
    {
        String duration = moduleFormatter.duration();
        if ( duration.length() > 0 )
        {
            return " built in " + duration;
        }
        return duration;
    }
}
