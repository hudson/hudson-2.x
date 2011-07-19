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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;
import org.eclipse.hudson.maven.model.state.ProfileDTO;

import java.util.Date;
import java.util.List;

/**
 * Standard formatting for Maven module information.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ModuleFormatter
{
    private final MavenProjectDTO module;

    public ModuleFormatter(MavenProjectDTO module) {
        this.module = module;
    }

    public String duration() {
        StringBuilder sb = new StringBuilder();
        if (ModuleInspector.hasDuration(module)) {
            sb.append(formatTime(module.getBuildSummary().getDuration()));
        }
        return sb.toString();
    }

    public String profiles() {
        List<ProfileDTO> profiles = module.getProfiles();

        StringBuilder sb = new StringBuilder();

        if (0 == profiles.size()) {
            sb.append("none");
        }
        else {
            for (ProfileDTO profile : profiles) {
                sb.append(profile.getId()).append(" ");
            }
            sb.append("(total: ").append(profiles.size()).append(")");
        }

        return sb.toString();
    }

    public String profiles(final boolean activeOnly) {
        List<ProfileDTO> profiles = module.getProfiles();

        StringBuilder sb = new StringBuilder();

        if (0 == profiles.size()) {
            sb.append("none");
        }
        else {
            int counted = 0;
            for (ProfileDTO profile : profiles) {
                if (activeOnly == false) {
                    sb.append(profile.getId()).append(" ");
                    counted++;
                }
                // Assume that activeOnly false has already been evaluated, don't need to check for
                // activeOnly == true.
                else if (profile.isActive()) {
                    sb.append(profile.getId()).append(" ");
                    counted++;
                }
            }
            if (0 == counted) {
                sb.append("none");
            }
            else {
                sb.append("(total: ").append(counted).append(")");
            }
        }

        return sb.toString();
    }

    public ImageResource statusIcon(BuildInfoResources resources) {
        BuildResultDTO result = module.getBuildSummary().getResult();
        return resolveStatusIcon(resources, result);
    }

    public static ImageResource resolveStatusIcon(BuildInfoResources resources, BuildResultDTO result) {
        switch (result) {
            case SUCCESS:
                return resources.buildSuccessIcon();
            case FAILURE:
                return resources.buildFailureIcon();
            case SKIPPED:
                return resources.buildDisabledIcon();
            case SCHEDULED:
                return resources.activityScheduled();
            case BUILDING:
                return resources.activityExecuting();
            default:
                return resources.buildDisabledIcon();
        }
    }

    /**
     * Adapted from org.apache.maven.cli.ExecutionEventLogger#getFormattedTime to be compatible with
     * GWT clients.
     */
    public static String formatTime(long time) {
        String pattern = "s.SSS's'";

        if (time / 60000L > 0) {
            pattern = "m:s" + pattern;

            if (time / 3600000L > 0) {
                pattern = "H:m" + pattern;
            }
        }

        return DateTimeFormat.getFormat(pattern).format(new Date(time));
    }
}
