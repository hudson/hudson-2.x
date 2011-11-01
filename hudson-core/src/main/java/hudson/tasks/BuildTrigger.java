/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Brian Westrich, Martin Eigenbrodt
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
package hudson.tasks;

import hudson.Launcher;
import hudson.Extension;
import hudson.Util;
import hudson.model.AutoCompletionCandidates;
import hudson.security.AccessControlled;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Cause.UpstreamCause;
import hudson.model.TaskListener;
import hudson.model.listeners.ItemListener;
import hudson.util.AutoCompleteSeeder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Triggers builds of other projects.
 *
 * <p>
 * Despite what the name suggests, this class doesn't actually trigger other jobs
 * as a part of {@link #perform} method. Its main job is to simply augument
 * {@link DependencyGraph}. Jobs are responsible for triggering downstream jobs
 * on its own, because dependencies may come from other sources.
 *
 * <p>
 * This class, however, does provide the {@link #execute(AbstractBuild, BuildListener, BuildTrigger)}
 * method as a convenience method to invoke downstream builds.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildTrigger extends Recorder implements DependecyDeclarer {

    /**
     * Comma-separated list of other projects to be scheduled.
     */
    private String childProjects;

    /**
     * Threshold status to trigger other builds.
     *
     * For compatibility reasons, this field could be null, in which case
     * it should read as "SUCCESS".
     */
    private final Result threshold;

    @DataBoundConstructor
    public BuildTrigger(String childProjects, boolean evenIfUnstable) {
        this(childProjects,evenIfUnstable ? Result.UNSTABLE : Result.SUCCESS);
    }

    public BuildTrigger(String childProjects, Result threshold) {
        if(childProjects==null)
            throw new IllegalArgumentException();
        this.childProjects = childProjects;
        this.threshold = threshold;
    }

    public BuildTrigger(List<AbstractProject> childProjects, Result threshold) {
        this((Collection<AbstractProject>)childProjects,threshold);
    }

    public BuildTrigger(Collection<? extends AbstractProject> childProjects, Result threshold) {
        this(Items.toNameList(childProjects),threshold);
    }

    public String getChildProjectsValue() {
        return childProjects;
    }

    public Result getThreshold() {
        if(threshold==null)
            return Result.SUCCESS;
        else
            return threshold;
    }

    public List<AbstractProject> getChildProjects() {
        return Items.fromNameList(childProjects,AbstractProject.class);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    /**
     * Checks if this trigger has the exact same set of children as the given list.
     */
    public boolean hasSame(Collection<? extends AbstractProject> projects) {
        List<AbstractProject> children = getChildProjects();
        return children.size()==projects.size() && children.containsAll(projects);
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        return true;
    }

    /**
     * @deprecated since 1.341; use {@link #execute(AbstractBuild,BuildListener)}
     */
    @Deprecated
    public static boolean execute(AbstractBuild build, BuildListener listener, BuildTrigger trigger) {
        return execute(build, listener);
    }

    /**
     * Convenience method to trigger downstream builds.
     *
     * @param build
     *      The current build. Its downstreams will be triggered.
     * @param listener
     *      Receives the progress report.
     */
    public static boolean execute(AbstractBuild build, BuildListener listener) {
        PrintStream logger = listener.getLogger();
        // Check all downstream Project of the project, not just those defined by BuildTrigger
        final DependencyGraph graph = Hudson.getInstance().getDependencyGraph();
        List<Dependency> downstreamProjects = new ArrayList<Dependency>(
                graph.getDownstreamDependencies(build.getProject()));
        // Sort topologically
        Collections.sort(downstreamProjects, new Comparator<Dependency>() {
            public int compare(Dependency lhs, Dependency rhs) {
                // Swapping lhs/rhs to get reverse sort:
                return graph.compare(rhs.getDownstreamProject(), lhs.getDownstreamProject());
            }
        });

        for (Dependency dep : downstreamProjects) {
            AbstractProject p = dep.getDownstreamProject();
            if (p.isDisabled()) {
                logger.println(Messages.BuildTrigger_Disabled(p.getName()));
                continue;
            }
            List<Action> buildActions = new ArrayList<Action>();
            if (dep.shouldTriggerBuild(build, listener, buildActions)) {
                // this is not completely accurate, as a new build might be triggered
                // between these calls
                String name = p.getName()+" #"+p.getNextBuildNumber();
                if(p.scheduleBuild(p.getQuietPeriod(), new UpstreamCause((Run)build),
                                   buildActions.toArray(new Action[buildActions.size()]))) {
                    logger.println(Messages.BuildTrigger_Triggering(name));
                } else {
                    logger.println(Messages.BuildTrigger_InQueue(name));
                }
            }
        }

        return true;
    }

    public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
        for (AbstractProject p : getChildProjects()) {
            if (!StringUtils.equals(p.getName(), owner.getName())) {
                graph.addDependency(new Dependency(owner, p) {
                    @Override
                    public boolean shouldTriggerBuild(AbstractBuild build, TaskListener listener,
                                                      List<Action> actions) {
                        return build.getResult().isBetterOrEqualTo(threshold);
                    }
                });
            }
        }
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    /**
     * Called from {@link hudson.tasks.BuildTrigger.DescriptorImpl.ItemListenerImpl} when a job is renamed.
     *
     * @return true if this {@link BuildTrigger} is changed and needs to be saved.
     */
    public boolean onJobRenamed(String oldName, String newName) {
        // quick test
        if(!childProjects.contains(oldName))
            return false;

        boolean changed = false;

        // we need to do this per string, since old Project object is already gone.
        String[] projects = childProjects.split(",");
        for( int i=0; i<projects.length; i++ ) {
            if(projects[i].trim().equals(oldName)) {
                projects[i] = newName;
                changed = true;
            }
        }

        if(changed) {
            StringBuilder b = new StringBuilder();
            for (String p : projects) {
                if(b.length()>0)    b.append(',');
                b.append(p);
            }
            childProjects = b.toString();
        }

        return changed;
    }

    /**
     * Correct broken data gracefully (#1537)
     */
    private Object readResolve() {
        if(childProjects==null)
            return childProjects="";
        return this;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return Messages.BuildTrigger_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/project-config/downstream.html";
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new BuildTrigger(
                formData.getString("childProjects"),
                formData.has("evenIfUnstable") && formData.getBoolean("evenIfUnstable"));
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public boolean showEvenIfUnstableOption(Class<? extends AbstractProject> jobType) {
            // UGLY: for promotion process, this option doesn't make sense. 
            return !jobType.getName().contains("PromotionProcess");
        }

        /**
         * Form validation method.
         */
        public FormValidation doCheck(@AncestorInPath AccessControlled subject, @AncestorInPath AbstractProject current,
                                      @QueryParameter String value ) {
            // Require CONFIGURE permission on this project
            if(!subject.hasPermission(Item.CONFIGURE))      return FormValidation.ok();

            StringTokenizer tokens = new StringTokenizer(Util.fixNull(value),",");
            while(tokens.hasMoreTokens()) {
                String projectName = tokens.nextToken().trim();
                Item item = Hudson.getInstance().getItemByFullName(projectName,Item.class);
                if(item==null)
                    return FormValidation.error(Messages.BuildTrigger_NoSuchProject(projectName,AbstractProject.findNearest(projectName).getName()));
                if(!(item instanceof AbstractProject))
                    return FormValidation.error(Messages.BuildTrigger_NotBuildable(projectName));
                if (StringUtils.equals(projectName, current.getName())) {
                    return FormValidation.error(Messages.BuildTrigger_FailedUsingCurrentProject());
                }
            }

            return FormValidation.ok();
        }

        public AutoCompletionCandidates doAutoCompleteChildProjectsValue(@QueryParameter String value) {
            AutoCompletionCandidates c = new AutoCompletionCandidates();
            List<Item> items = Hudson.getInstance().getItems(Item.class);
            List<String> queries = new AutoCompleteSeeder(value).getSeeds();

            for (String term : queries) {
                for (Item item : items) {
                    if (item.getName().startsWith(term)) {
                        c.add(item.getName());
                    }
                }
            }
            return c;
        }

        @Extension
        public static class ItemListenerImpl extends ItemListener {
            @Override
            public void onRenamed(Item item, String oldName, String newName) {
                // update BuildTrigger of other projects that point to this object.
                // can't we generalize this?
                for( Project<?,?> p : Hudson.getInstance().getProjects() ) {
                    BuildTrigger t = p.getPublishersList().get(BuildTrigger.class);
                    if(t!=null) {
                        if(t.onJobRenamed(oldName,newName)) {
                            try {
                                p.save();
                            } catch (IOException e) {
                                LOGGER.log(Level.WARNING, "Failed to persist project setting during rename from "+oldName+" to "+newName,e);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BuildTrigger.class.getName());
}
