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

package org.eclipse.hudson.maven.plugin.builder.internal;

import org.eclipse.hudson.maven.model.PropertiesDTO;
import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;
import org.eclipse.hudson.maven.model.config.ChecksumModeDTO;
import org.eclipse.hudson.maven.model.config.FailModeDTO;
import org.eclipse.hudson.maven.model.config.MakeModeDTO;
import org.eclipse.hudson.maven.model.config.SnapshotUpdateModeDTO;
import org.eclipse.hudson.maven.model.config.VerbosityDTO;
import net.sf.json.JSONObject;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.maven.model.ModelUtil.parseList;
import static org.eclipse.hudson.maven.model.ModelUtil.parseProperties;

/**
 * Helper to extract build configuration details from a {@link JSONObject}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BuildConfigurationExtractor
{
    private final JSONObject data;

    public BuildConfigurationExtractor(final JSONObject data) {
        this.data = checkNotNull(data);
    }

    private String getString(final String name) {
        return nullIfEmpty(data.getString(name));
    }

    private boolean getBoolean(final String name) {
        return data.getBoolean(name);
    }

    private PropertiesDTO getProperties(final String name) {
        return parseProperties(data.getString(name));
    }

    private List<String> getList(final String name) {
        return parseList(nullIfEmpty(data.getString(name)));
    }

    private <E extends Enum<E>> E getEnum(final Class<E> type, final String name) {
        return Enum.valueOf(type, data.getString(name));
    }

    private String nullIfEmpty(final String value) {
        if (value != null && value.trim().length() == 0) {
            return null;
        }
        return value;
    }

    private String nullIfEmptyOrNone(final String value) {
        if (nullIfEmpty(value) == null || "NONE".equals(value)) {
            return null;
        }
        return value;
    }

    public BuildConfigurationDTO extract() {
        BuildConfigurationDTO config = new BuildConfigurationDTO();
        config.setInstallationId(getString("installationId"));
        config.setGoals(getString("goals"));
        config.setProperties(getProperties("properties"));
        config.setPomFile(getString("pomFile"));
        config.setPrivateRepository(getBoolean("privateRepository"));
        config.setPrivateTmpdir(getBoolean("privateTmpdir"));
        config.setOffline(getBoolean("offline"));
        config.setRecursive(getBoolean("recursive"));
        config.withProfiles(getList("profiles"));
        config.withProjects(getList("projects"));
        config.setResumeFrom(getString("resumeFrom"));
        config.setErrors(getBoolean("errors"));
        config.setVerbosity(getEnum(VerbosityDTO.class, "verbosity"));
        config.setChecksumMode(getEnum(ChecksumModeDTO.class, "checksumMode"));
        config.setFailMode(getEnum(FailModeDTO.class, "failMode"));
        config.setMakeMode(getEnum(MakeModeDTO.class, "makeMode"));
        config.setSnapshotUpdateMode(getEnum(SnapshotUpdateModeDTO.class, "snapshotUpdateMode"));
        config.setThreading(getString("threading"));
        config.setMavenOpts(getString("mavenOpts"));
        config.setSettingsId(getString("settingsId"));
        config.setGlobalSettingsId(getString("globalSettingsId"));
        config.setToolChainsId(getString("toolChainsId"));
        return config;
    }
}
