/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi, Olivier Lamy
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Extension;

import java.io.Serializable;

import hudson.Functions;

/**
 * group id + artifact id + version and a flag to know if it's a plugin 
 *
 * @author Kohsuke Kawaguchi
 * @see ModuleName
 */
public class ModuleDependency implements Serializable {
    public final String groupId;
    public final String artifactId;
    public final String version;
    
    /**
     * @since 1.395
     */
    public boolean plugin = false;

    public ModuleDependency(String groupId, String artifactId, String version) {
        this.groupId = groupId.intern();
        this.artifactId = artifactId.intern();
        if(version==null)   version=UNKNOWN;
        this.version = version.intern();
    }

    public ModuleDependency(ModuleName name, String version) {
        this(name.groupId,name.artifactId,version);
    }

    public ModuleDependency(org.apache.maven.model.Dependency dep) {
        this(dep.getGroupId(),dep.getArtifactId(),dep.getVersion());
    }

    public ModuleDependency(MavenProject project) {
        this(project.getGroupId(),project.getArtifactId(),project.getVersion());
    }

    public ModuleDependency(Plugin p) {
        this(p.getGroupId(),p.getArtifactId(), Functions.defaulted(p.getVersion(),NONE));
        this.plugin = true;
    }

    public ModuleDependency(ReportPlugin p) {
        this(p.getGroupId(),p.getArtifactId(),p.getVersion());
        this.plugin = true;
    }

    public ModuleDependency(Extension ext) {
        this(ext.getGroupId(),ext.getArtifactId(),ext.getVersion());
    }

    public ModuleName getName() {
        return new ModuleName(groupId,artifactId);
    }

    /**
     * Returns groupId+artifactId plus unknown version.
     */
    public ModuleDependency withUnknownVersion() {
        return new ModuleDependency(groupId,artifactId,UNKNOWN);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleDependency that = (ModuleDependency) o;

        return this.artifactId.equals(that.artifactId)
            && this.groupId.equals(that.groupId)
            && this.version.equals(that.version)
            && this.plugin == that.plugin;
    }

    public int hashCode() {
        int result;
        result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (plugin ? 1 : 2);
        return result;
    }

    /**
     * Upon reading from the disk, intern strings.
     */
    public ModuleDependency readResolve() {
        return new ModuleDependency(groupId,artifactId,version);
    }

    /**
     * For compatibility reason, this value may be used in the verion field
     * to indicate that the version is unknown.
     */
    public static final String UNKNOWN = "*";

    /**
     * When a plugin dependency is specified without giving a version,
     * the semantics of that is the latest released plugin.
     * In this case, we don't want the {@link ModuleDependency} version to become
     * {@link #UNKNOWN}, which would match any builds of the plugin.
     *
     * <p>
     * So we use this constant to indicate a version, and this will not match
     * anything.
     *
     * @see #ModuleDependency(Plugin)
     */
    public static final String NONE = "-";

    private static final long serialVersionUID = 1L;
}
