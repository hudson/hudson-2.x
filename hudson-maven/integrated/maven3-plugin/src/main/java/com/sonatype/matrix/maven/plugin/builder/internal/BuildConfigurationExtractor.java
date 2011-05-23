/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import com.sonatype.matrix.maven.model.PropertiesDTO;
import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import com.sonatype.matrix.maven.model.config.ChecksumModeDTO;
import com.sonatype.matrix.maven.model.config.FailModeDTO;
import com.sonatype.matrix.maven.model.config.MakeModeDTO;
import com.sonatype.matrix.maven.model.config.SnapshotUpdateModeDTO;
import com.sonatype.matrix.maven.model.config.VerbosityDTO;
import net.sf.json.JSONObject;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.matrix.maven.model.ModelUtil.parseList;
import static com.sonatype.matrix.maven.model.ModelUtil.parseProperties;

/**
 * Helper to extract build configuration details from a {@link JSONObject}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

    private String getId(final String name) {
        return nullIfEmptyOrNone(data.getString(name));
    }

    private PropertiesDTO getProperties(final String name) {
        return parseProperties(nullIfEmpty(data.getString(name)));
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
        config.setInstallationId(getId("installationId"));
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
        config.setSettingsId(getId("settingsId"));
        config.setGlobalSettingsId(getId("globalSettingsId"));
        config.setToolChainsId(getId("toolChainsId"));
        return config;
    }
}