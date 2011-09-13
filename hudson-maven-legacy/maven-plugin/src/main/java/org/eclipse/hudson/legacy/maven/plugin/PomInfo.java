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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.project.MavenProject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * Serializable representation of the key information obtained from Maven POM.
 *
 * <p>
 * This is used for the master to introspect POM, which is only available
 * as {@link MavenProject} object on slaves.
 *
 * @author Kohsuke Kawaguchi
 */
public final class PomInfo implements Serializable {

    public final ModuleName name;

    /**
     * This is a human readable name of the POM. Not necessarily unique
     * or file system safe.
     *
     * @see MavenProject#getName() 
     */
    public final String displayName;

    /**
     * Relative path from the root directory of the root POM to
     * the root directory of this module.
     *
     * Strings like "" (if this is the root), "abc", "foo/bar/zot".
     */
    public final String relativePath;

    /**
     * Version number taken from POM.
     *
     * @see MavenProject#getVersion()
     */
    public final String version;

    /**
     * Dependency of this project.
     *
     * See Maven's ProjectSorter class for the definition of the 'dependencies' in Maven.
     */
    public final Set<ModuleDependency> dependencies = new HashSet<ModuleDependency>();

    /**
     * Children of this module.
     */
    public final List<ModuleName> children = new ArrayList<ModuleName>();

    /**
     * The default goal specified in POM or null.
     */
    public final String defaultGoal;

    /**
     * Parent module.
     */
    public final PomInfo parent;
    
    /**
     * maven groupId
     */
    private final String groupId;
    
    /**
     * maven artifactId
     */    
    private final String artifactId;

    public final Notifier mailNotifier;

    public PomInfo(MavenProject project, PomInfo parent, String relPath) {
        this.name = new ModuleName(project);
        this.version = project.getVersion();
        this.displayName = project.getName();
        this.defaultGoal = project.getDefaultGoal();
        this.relativePath = relPath;
        this.parent = parent;
        if(parent!=null)
            parent.children.add(name);

        for (Dependency dep : (List<Dependency>)project.getDependencies())
            dependencies.add(new ModuleDependency(dep));

        MavenProject parentProject = project.getParent();
        if(parentProject!=null)
            dependencies.add(new ModuleDependency(parentProject));
        if(parent!=null)
            dependencies.add(parent.asDependency());

        addPluginsAsDependencies(project.getBuildPlugins(),dependencies);
        addReportPluginsAsDependencies(project.getReportPlugins(),dependencies);

        List<Extension> extensions = project.getBuildExtensions();
        if(extensions!=null)
            for (Extension ext : extensions)
                dependencies.add(new ModuleDependency(ext));

        // when the parent POM uses a plugin and builds a plugin at the same time,
        // the plugin module ends up depending on itself
        dependencies.remove(asDependency());

        CiManagement ciMgmt = project.getCiManagement();
        if ((ciMgmt != null) && (ciMgmt.getSystem()==null || ciMgmt.getSystem().equals("hudson"))) {
            Notifier mailNotifier = null;
            for (Notifier n : (List<Notifier>)ciMgmt.getNotifiers()) {
                if (n.getType().equals("mail")) {
                    mailNotifier = n;
                    break;
                }
            }
            this.mailNotifier = mailNotifier;
        } else
            this.mailNotifier = null;
        
        this.groupId = project.getGroupId();
        this.artifactId = project.getArtifactId();
    }

    /**
     * Creates {@link ModuleDependency} that represents this {@link PomInfo}.
     */
    private ModuleDependency asDependency() {
        return new ModuleDependency(name,version);
    }

    private void addPluginsAsDependencies(List<Plugin> plugins, Set<ModuleDependency> dependencies) {
        if(plugins==null)   return;
        for (Plugin p : plugins)
            dependencies.add(new ModuleDependency(p));
    }

    private void addReportPluginsAsDependencies(List<ReportPlugin> plugins, Set<ModuleDependency> dependencies) {
        if(plugins==null)   return;
        for (ReportPlugin p : plugins)
            dependencies.add(new ModuleDependency(p));
    }

    /**
     * Avoids dependency cycles.
     *
     * <p>
     * People often write configuration in parent POMs that use the plugin
     * which is a part of the build. To avoid this kind of dependency,
     * make sure parent POMs don't depend on a child module.
     */
    /*package*/ void cutCycle() {
        ModuleDependency dep = asDependency();
        for(PomInfo p=parent; p!=null; p=p.parent) {
            if(p.dependencies.contains(dep))
                p.dependencies.remove(dep);
        }
    }

    /**
     * Computes the number of ancestors of this POM.
     * returns 0 if this is the top-level module.
     */
    public int getNestLevel() {
        int i=0;
        for(PomInfo p=parent; p!=null; p=p.parent)
            i++;
        return i;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public int hashCode()
    {
        int hash = 23 + this.groupId == null ? 1 : this.groupId.hashCode();
        hash += this.artifactId == null ? 1 : this.artifactId.hashCode();
        return hash;
    }

    @Override
    public boolean equals( Object obj ){
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PomInfo)) {
            return false;
        }
        PomInfo pomInfo = (PomInfo) obj;
        if (!StringUtils.equals( pomInfo.groupId, this.groupId )){
            return false;
        }
        if (!StringUtils.equals( pomInfo.artifactId, this.artifactId )){
            return false;
        }
        return isDependencisesSimilar(pomInfo);
    }

    private boolean isDependencisesSimilar(PomInfo pom){

        if (pom.dependencies.size() != dependencies.size()){
            return false;
        }
        for (ModuleDependency dependency : dependencies){
            // This is feasible because ModuleDependency overrides equal and Hashcode
            if (!pom.dependencies.contains(dependency)){
                return false;
            }
        }
        return true;
    }
    
    /**
     * for debug purpose
     */
    public String toString() {
        return "PomInfo:["+groupId+':'+artifactId+']'+"[relativePath:"+relativePath+']';
    }    
}
