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

package org.eclipse.hudson.utils.tasks;

import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.Project;
import hudson.security.Permission;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.util.DescribableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a unified interface for {@link AbstractProject} types.
 * 
 * Avoiding generic types where possible to avoid <tt>"inconvertible types" due to capture###</tt>.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@SuppressWarnings( {"rawtypes", "unchecked"} )
public class MetaProject
{
    private static final Logger log = LoggerFactory.getLogger(MetaProject.class);

    private final AbstractProject delegate;

    public static enum Type
    {
        /**
         * Any other project type (like MavenModuleSet, etc).
         */
        UNSUPPORTED,

        /**
         * Sub-class of {@link Project}.
         */
        NORMAL,

        /**
         * Sub-class of {@link MatrixProject}.
         */
        MULTICONFIG
    }
    
    private final Type type;

    public MetaProject(final AbstractProject project) {
        this.delegate = checkNotNull(project);

        if (delegate instanceof MatrixProject) {
            type = Type.MULTICONFIG;
        }
        else if (delegate instanceof Project) {
            type = Type.NORMAL;
        }
        else {
            type = Type.UNSUPPORTED;
            log.debug("Unsupported project type: {}", project.getClass().getName());
        }
    }

    public AbstractProject getDelegate() {
        return delegate;
    }

    public Type getType() {
        return type;
    }

    public boolean isSupported() {
        return getType() != Type.UNSUPPORTED;
    }

    public boolean isNormal() {
        return getType() == Type.NORMAL;
    }

    public boolean isMultiConfig() {
        return getType() == Type.MULTICONFIG;
    }

    @Override
    public String toString() {
        return String.format("%s (%s,%s)", getFullDisplayName(), getType(), getId());
    }

    /**
     * Typed access to project instance as a normal {@link Project}.
     */
    public Project<?,?> asNormal() {
        return (Project)getDelegate();
    }

    /**
     * Typed access to project instance as a multi-config {@link MatrixProject}.
     */
    public MatrixProject asMultiConfig() {
        return (MatrixProject)getDelegate();
    }

    public ItemGroup getParent() {
        return getDelegate().getParent();
    }

    public String getName() {
        return getDelegate().getName();
    }

    public String getFullName() {
        return getDelegate().getFullName();
    }

    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    public String getFullDisplayName() {
        return getDelegate().getFullDisplayName();
    }

    public UUID getId() {
        return JobUuid.get(getDelegate());
    }

    public void setEnabled(final boolean enabled) throws IOException {
        getDelegate().makeDisabled(!enabled);
    }

    public boolean isEnabled() {
        return !getDelegate().isDisabled();
    }

    public Collection<JobProperty> getProperties() {
        return getDelegate().getAllProperties();
    }

    public void addProperty(final JobProperty item) throws IOException {
        checkNotNull(item);
        getDelegate().addProperty(item);
    }

    public Collection<Trigger> getTriggers() {
        return getDelegate().getTriggers().values();
    }
    
    public void addTrigger(final Trigger item) throws IOException {
        checkNotNull(item);
        getDelegate().addTrigger(item);
    }

    /**
     * @throws UnsupportedProjectException if the Project type is {@link Type#UNSUPPORTED}.
     */
    public DescribableList<BuildWrapper,Descriptor<BuildWrapper>> getBuildWrappersList() {
        switch (getType()) {
            case NORMAL:
                return asNormal().getBuildWrappersList();

            case MULTICONFIG:
                return asMultiConfig().getBuildWrappersList();
        }

        throw new UnsupportedProjectException(getDelegate());
    }

    /**
     * @throws UnsupportedProjectException if the Project type is {@link Type#UNSUPPORTED}.
     */
    public Collection<BuildWrapper> getBuildWrappers() {
        return getBuildWrappersList().toList();
    }

    /**
     * @throws UnsupportedProjectException if the Project type is {@link Type#UNSUPPORTED}.
     */
    public DescribableList<Builder,Descriptor<Builder>> getBuildersList() {
        switch (getType()) {
            case NORMAL:
                return asNormal().getBuildersList();

            case MULTICONFIG:
                return asMultiConfig().getBuildersList();
        }

        throw new UnsupportedProjectException(getDelegate());
    }

    /**
     * @throws UnsupportedProjectException if the Project type is {@link Type#UNSUPPORTED}.
     */
    public Collection<Builder> getBuilders() {
        return getBuildersList().toList();
    }

    /**
     * @throws UnsupportedProjectException if the Project type is {@link Type#UNSUPPORTED}.
     */
    public DescribableList<Publisher,Descriptor<Publisher>> getPublishersList() {
        switch (getType()) {
            case NORMAL:
                return asNormal().getPublishersList();

            case MULTICONFIG:
                return asMultiConfig().getPublishersList();
        }

        throw new UnsupportedProjectException(getDelegate());
    }

    /**
     * @throws UnsupportedProjectException if the Project type is {@link Type#UNSUPPORTED}.
     */
    public Collection<Publisher> getPublishers() {
        return getPublishersList().toList();
    }

    public void checkPermission(final Permission perm) {
        getDelegate().checkPermission(perm);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        MetaProject that = (MetaProject) obj;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    // FIXME: Extract out to service

    public static interface Filter
    {
        /**
         * Return true to include the given project in the result.
         *
         * @param project   Never null
         */
        boolean accept(MetaProject project);
    }

    public static Collection<MetaProject> list(final Filter filter) {
        List<MetaProject> projects = new ArrayList<MetaProject>();

        for (Item item : Hudson.getInstance().getItems()) {
            if (item instanceof AbstractProject) {
                MetaProject project = new MetaProject((AbstractProject)item);
                if (filter == null || filter.accept(project)) {
                    projects.add(project);
                }
            }
        }

        return projects;
    }

    public static Collection<MetaProject> list() {
        return list(null);
    }

    public static MetaProject find(final UUID id) {
        checkNotNull(id);
        Job job = JobUuid.find(id);
        if (job instanceof AbstractProject) {
            return new MetaProject((AbstractProject)job);
        }
        return null;
    }

    public static MetaProject find(final String id) {
        checkNotNull(id);
        return find(UUID.fromString(id));
    }
}
