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

package org.hudsonci.maven.plugin.builder.internal;

import org.hudsonci.maven.model.PropertiesDTO;
import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.maven.model.config.ChecksumModeDTO;
import org.hudsonci.maven.model.config.FailModeDTO;
import org.hudsonci.maven.model.config.MakeModeDTO;
import org.hudsonci.maven.model.config.SnapshotUpdateModeDTO;
import org.hudsonci.maven.model.config.VerbosityDTO;
import net.sf.json.JSONObject;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.maven.model.ModelUtil.parseList;
import static org.hudsonci.maven.model.ModelUtil.parseProperties;

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
