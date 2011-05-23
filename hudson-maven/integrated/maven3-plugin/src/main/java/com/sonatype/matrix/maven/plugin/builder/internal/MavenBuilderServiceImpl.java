/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import com.google.common.collect.Lists;
import com.sonatype.matrix.MetaProject;
import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.plugin.builder.BuildStateNotFoundException;
import com.sonatype.matrix.maven.plugin.builder.BuildStateRecord;
import com.sonatype.matrix.maven.plugin.builder.BuilderConfigurationNotFoundException;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilder;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderDescriptor;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderService;
import com.sonatype.matrix.service.BuildService;
import com.sonatype.matrix.service.DescriptorService;
import com.sonatype.matrix.service.ProjectService;
import com.sonatype.matrix.service.SecurityService;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.tasks.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link MavenBuilderService}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
@Singleton
public class MavenBuilderServiceImpl
    implements MavenBuilderService
{
    private static final Logger log = LoggerFactory.getLogger(MavenBuilderServiceImpl.class);

    private final SecurityService security;

    private final DescriptorService descriptors;

    private final ProjectService projects;

    private final BuildService builds;

    @Inject
    public MavenBuilderServiceImpl(final SecurityService security,
                                   final DescriptorService descriptors,
                                   final ProjectService projects,
                                   final BuildService builds)
    {
        this.security = checkNotNull(security);
        this.descriptors = checkNotNull(descriptors);
        this.projects = checkNotNull(projects);
        this.builds = checkNotNull(builds);
    }

    private MavenBuilderDescriptor getMavenBuilderDescriptor() {
        return descriptors.getDescriptorByType(MavenBuilderDescriptor.class);
    }

    @Override
    public BuildConfigurationDTO getBuilderDefaultConfiguration() {
        security.checkPermission(Hudson.ADMINISTER);
        return getMavenBuilderDescriptor().getDefaults();
    }

    @Override
    public void setBuilderDefaultConfiguration(BuildConfigurationDTO defaults) {
        checkNotNull(defaults);
        security.checkPermission(Hudson.ADMINISTER);
        log.debug("Set builder default config: {}", defaults);
        getMavenBuilderDescriptor().setDefaults(defaults);
    }

    @Override
    public void resetBuilderDefaultConfiguration() {
        security.checkPermission(Hudson.ADMINISTER);
        log.debug("Reset builder default config");
        getMavenBuilderDescriptor().setDefaults(MavenBuilderDescriptor.DEFAULTS);
    }

    @Override
    public List<BuildConfigurationDTO> getBuilderConfigurations(final String projectName) {
        checkNotNull(projectName);

        log.debug("Fetching builder configs: {}", projectName);

        MetaProject project = new MetaProject(projects.getProject(projectName));
        project.checkPermission(Item.CONFIGURE);

        List<MavenBuilder> builders = project.getBuildersList().getAll(MavenBuilder.class);
        List<BuildConfigurationDTO> configs = Lists.newArrayListWithCapacity(builders.size());

        for (MavenBuilder builder : builders) {
            configs.add(builder.getConfig());
        }

        return configs;
    }

    @Override
    public BuildConfigurationDTO getBuilderConfiguration(final String projectName, final int index) {
        checkNotNull(projectName);
        checkArgument(index >= 0);

        log.debug("Fetching builder config: {} [{}]", projectName, index);

        List<BuildConfigurationDTO> configs = getBuilderConfigurations(projectName);
        try {
            return configs.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            throw new BuilderConfigurationNotFoundException(projectName, index);
        }
    }

    @Override
    public void setBuilderConfiguration(final String projectName, final int index, final BuildConfigurationDTO config) throws IOException {
        checkNotNull(projectName);
        checkArgument(index >= 0);
        checkNotNull(config);

        log.debug("Setting builder config: {} [{}]", projectName, index);

        MetaProject project = new MetaProject(projects.getProject(projectName));
        project.checkPermission(Item.CONFIGURE);

        List<Builder> builders = Lists.newArrayList();
        int i=0;
        boolean found = false;

        // Walk through the builders and find the maven builder at index, replace its configuration & rebuild a new list in the same order
        for (Builder builder : project.getBuilders()) {
            if (builder instanceof MavenBuilder) {
                if (i == index) {
                    builder = new MavenBuilder(config);
                    found = true;
                }
                i++;
            }
            builders.add(builder);
        }

        // If we didn't find the builder, then puke
        if (!found) {
            throw new BuilderConfigurationNotFoundException(projectName, index);
        }

        // Save the new builders list
        project.getBuildersList().replaceBy(builders);
    }

    @Override
    public List<BuildStateDTO> getBuildStates(final String projectName, final int buildNumber) {
        checkNotNull(projectName);
        checkArgument(buildNumber > 0);

        log.debug("Fetching build states: {} #{}", projectName, buildNumber);

        AbstractBuild build = builds.getBuild(projectName, buildNumber);
        List<BuildStateDTO> states = getBuildStates(build);

        log.debug("Found {} build states", states.size());

        return states;
    }

    @Override
    public List<BuildStateDTO> getBuildStates(final AbstractBuild build) {
        build.checkPermission(Item.READ);
        List<BuildStateDTO> states = Lists.newArrayList();

        for (BuildStateRecord record : build.getActions(BuildStateRecord.class)) {
            states.add(record.getState());
        }
        return states;
    }

    @Override
    public BuildStateDTO getBuildState(String projectName, int buildNumber, int index) {
        checkArgument(index >= 0);

        List<BuildStateDTO> states = getBuildStates(projectName, buildNumber);
        try {
            return states.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            throw new BuildStateNotFoundException(projectName, buildNumber, index);
        }
    }
}