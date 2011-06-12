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

import com.google.common.collect.Lists;
import org.hudsonci.utils.tasks.MetaProject;
import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.service.BuildService;
import org.hudsonci.service.DescriptorService;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.tasks.Builder;

import org.hudsonci.maven.plugin.builder.BuildStateNotFoundException;
import org.hudsonci.maven.plugin.builder.BuildStateRecord;
import org.hudsonci.maven.plugin.builder.BuilderConfigurationNotFoundException;
import org.hudsonci.maven.plugin.builder.MavenBuilder;
import org.hudsonci.maven.plugin.builder.MavenBuilderDescriptor;
import org.hudsonci.maven.plugin.builder.MavenBuilderService;
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
 * @since 2.1.0
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

    public BuildConfigurationDTO getBuilderDefaultConfiguration() {
        security.checkPermission(Hudson.ADMINISTER);
        return getMavenBuilderDescriptor().getDefaults();
    }

    public void setBuilderDefaultConfiguration(BuildConfigurationDTO defaults) {
        checkNotNull(defaults);
        security.checkPermission(Hudson.ADMINISTER);
        log.debug("Set builder default config: {}", defaults);
        getMavenBuilderDescriptor().setDefaults(defaults);
    }

    public void resetBuilderDefaultConfiguration() {
        security.checkPermission(Hudson.ADMINISTER);
        log.debug("Reset builder default config");
        getMavenBuilderDescriptor().setDefaults(MavenBuilderDescriptor.DEFAULTS);
    }

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

    public List<BuildStateDTO> getBuildStates(final String projectName, final int buildNumber) {
        checkNotNull(projectName);
        checkArgument(buildNumber > 0);

        log.debug("Fetching build states: {} #{}", projectName, buildNumber);

        AbstractBuild build = builds.getBuild(projectName, buildNumber);
        List<BuildStateDTO> states = getBuildStates(build);

        log.debug("Found {} build states", states.size());

        return states;
    }

    public List<BuildStateDTO> getBuildStates(final AbstractBuild build) {
        build.checkPermission(Item.READ);
        List<BuildStateDTO> states = Lists.newArrayList();

        for (BuildStateRecord record : build.getActions(BuildStateRecord.class)) {
            states.add(record.getState());
        }
        return states;
    }

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
