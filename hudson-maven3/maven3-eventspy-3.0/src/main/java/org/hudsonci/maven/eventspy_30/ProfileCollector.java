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

package org.hudsonci.maven.eventspy_30;

import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts {@link Profile}s from a {@link MavenProject} and converts them to {@link ResolvedProfile}s.
 * 
 * Currently extracts pom and external profiles, but not those from the super pom.
 * 
 * Benjamin's comments regarding the usefulness of super pom profiles:
 * There is currently only one profile in the super POM (release-profile) which 
 * is scheduled for removal in future versions as users should rather set up 
 * their own release profile according their tastes. Likewise, I don't see us 
 * adding any other profiles to the super POM.
 * 
 * Note: the super pom has an empty string for the key and is always present
 * but may not have any profiles. It will only be added if there are profiles.
 * TODO: may want to make this a special string similar to 'external', 'super-pom' or the GAV?
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ProfileCollector
{
    private final MavenProject initiatingProject;

    private final Collection<String> activeProfiles;

    private final Collection<ResolvedProfile> container;

    /**
     * Non-instantiable, use {@link #collect(MavenProject)}.
     */
    private ProfileCollector( MavenProject project )
    {
        this.initiatingProject = project;
        this.activeProfiles = getActiveProfileIds( project.getInjectedProfileIds() );
        this.container = new ArrayList<ResolvedProfile>();
    }

    /**
     * Collects the {@link ResolvedProfile}s from the {@link MavenProject} up through
     * all parent hierarchies and any external (settings.xml) profiles.
     * 
     * @param project the starting project
     * @return The profiles within this projects hierarchy, never {@code null}.
     * 
     * @see MavenProject#getInjectedProfileIds() for similar information.
     */
    public static Collection<ResolvedProfile> collect( final MavenProject project )
    {
        checkNotNull(project);
        return new ProfileCollector( project ).execute();
    }

    private Collection<ResolvedProfile> execute()
    {
        collectExternal();
        collectFromProjectUp( initiatingProject );
        // TODO: super pom, source identified as empty string.

        return container;
    }

    /**
     * ProjectBuildingRequest().getProfiles() contains the 'external' profiles present for this project.
     * TODO: Consider passing in the 'external' list rather than using ProjectBuildingRequest.
     * This could come from ExecutionEvent.getSession().getRequest which is a MavenExecutionRequest,
     */
    private void collectExternal()
    {
        ProjectBuildingRequest projectBuildingRequest = initiatingProject.getProjectBuildingRequest();
        if ( null != projectBuildingRequest )
        {
            collectResolvedProfiles( ResolvedProfile.EXTERNAL, projectBuildingRequest.getProfiles() );
        }
    }

    private void collectFromProjectUp( final MavenProject project )
    {
        // At the top of the hierarchy, stop recursion.
        if ( null == project )
        {
            return;
        }

        collectResolvedProfiles( project, project.getModel().getProfiles() );
        // Walk up hierarchy.
        collectFromProjectUp( project.getParent() );
    }

    private void collectResolvedProfiles( final MavenProject source, final Collection<Profile> profiles )
    {
        for ( Profile profile : profiles )
        {
            container.add( new ResolvedProfile( source, profile, isActive( profile ) ) );
        }
    }

    /**
     * If Maven thinks the ResolvedProfile is active, make it so.
     * 
     * Need to perform operation outside of ResolvedProfile because external profiles
     * will not have a MavenProject to get the InjectedProfileIds from.
     * 
     * @param profile the ResolvedProfiles backing Profile
     * @return true if the profile is active, false otherwise
     */
    private boolean isActive( final Profile profile )
    {
        return activeProfiles.contains( profile.getId() );
    }

    private Collection<String> getActiveProfileIds( final Map<String, List<String>> activeProfilesMap )
    {
        Collection<String> allActiveProfiles = new ArrayList<String>();
        Collection<List<String>> values = activeProfilesMap.values();
        for ( List<String> list : values )
        {
            allActiveProfiles.addAll( list );
        }
        return allActiveProfiles;
    }
}
