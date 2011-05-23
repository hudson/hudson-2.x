/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

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
 * @since 1.1
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