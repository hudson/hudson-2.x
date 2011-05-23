/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.internal;

import org.sonatype.licensing.feature.AbstractFeature;
import org.sonatype.licensing.feature.Feature;

import javax.inject.Named;
import javax.inject.Singleton;

import static com.sonatype.matrix.license.internal.HudsonProFeature.ID_PREFIX;

/**
 * Maven feature.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named(MavenFeature.ID)
@Singleton
public class MavenFeature
    extends AbstractFeature
{
    public static final Feature INSTANCE = new MavenFeature();

    public static final String ID = ID_PREFIX + "maven";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Maven Integration";
    }

    @Override
    public String getDescription() {
        return "Maven Integration";
    }

    @Override
    public String getShortName() {
        return "maven";
    }
}