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
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.project.MavenProject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Source identifier has the form {@code <groupId>:<artifactId>:<version>} to be consistent
 * with {@link MavenProject#getInjectedProfileIds()}.
 * 
 * {@link ModelBuildingResult#getModelIds()} is the backing format for MavenProjects.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ResolvedProfile
{
    public static final MavenProject EXTERNAL = null;

    private final MavenProject declaringProject;

    private final Profile profile;

    private final boolean isActive;

    /**
     * If the Profiles are from an external source use {@link ResolvedProfile#EXTERNAL} as the declaringProject.
     * 
     * @param source the project that originated the profile, or {@link ResolvedProfile#EXTERNAL}
     * @param profile
     * @param isActive
     */
    public ResolvedProfile( MavenProject source, Profile profile, boolean isActive )
    {
        // MavenProject can be null for the special case of external profiles.
        checkNotNull( profile );

        // TODO: Consider extracting the sourceId and profileId at time of construction
        // to mitigate mutability and extraneous object references.
        this.declaringProject = source;
        this.profile = profile;
        this.isActive = isActive;
    }

    public String getId()
    {
        return profile.getId();
    }

    public String getSource()
    {
        String sourceId;
        // Using null as a special case for external/settings.xml based profiles.
        if ( null == declaringProject )
        {
            sourceId = "external";
        }
        else
        {
            // NOTE MavenProject.getId is in a different format than MavenProject.getInjectedProfileIds
            sourceId =
                String.format( "%s:%s:%s", declaringProject.getGroupId(), declaringProject.getArtifactId(),
                               declaringProject.getVersion() );
        }

        return sourceId;
    }

    public boolean isActive()
    {
        return isActive;
    }
}
