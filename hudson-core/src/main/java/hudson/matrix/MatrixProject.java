/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Jorg Heymans,
 * Red Hat, Inc., id:cactusman, Anton Kozak, Nikita Levyankov
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
package hudson.matrix;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.DependencyGraph;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.Label;
import hudson.model.Queue.FlyweightTask;
import hudson.model.Result;
import hudson.model.SCMedItem;
import hudson.model.Saveable;
import hudson.model.TopLevelItem;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.util.CopyOnWriteMap;
import hudson.util.DescribableList;
import hudson.util.DescribableListUtil;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.hudsonci.api.matrix.IMatrixProject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.TokenList;

/**
 * {@link Job} that allows you to run multiple different configurations
 * from a single setting.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixProject extends AbstractProject<MatrixProject, MatrixBuild> implements IMatrixProject, TopLevelItem,
    SCMedItem, ItemGroup<MatrixConfiguration>, Saveable, FlyweightTask, BuildableItemWithBuildWrappers {

    public static final String HAS_COMBINATION_FILTER_PARAM = "hasCombinationFilter";
    public static final String HAS_TOUCH_STONE_COMBINATION_FILTER_PARAM = "hasTouchStoneCombinationFilter";
    public static final String TOUCH_STONE_COMBINATION_FILTER_PARAM = "touchStoneCombinationFilter";
    public static final String TOUCH_STONE_RESULT_CONDITION_PARAM = "touchStoneResultCondition";
    public static final String CUSTOM_WORKSPACE_PARAM = "customWorkspace";
    public static final String CUSTOM_WORKSPACE_DIRECTORY_PARAM = "customWorkspace.directory";
    public static final String RUN_SEQUENTIALLY_PROPERTY_NAME = "runSequentially";
    public static final String COMBINATION_FILTER_PROPERTY_NAME = "combinationFilter";
    public static final String TOUCH_STONE_COMBINATION_FILTER_PROPERTY_NAME = "touchStoneCombinationFilter";
    public static final String TOUCH_STONE_RESULT_CONDITION_PROPERTY_NAME = "touchStoneResultCondition";
    public static final String AXES_PROPERTY_NAME = "axes";

    /**
     * Configuration axes.
     * @deprecated as of 2.2.0, use #getAxes() and #setAxes() instead
     */
    @Deprecated
    private volatile AxisList axes = new AxisList();

    /**
     * The filter that is applied to combinations. It is a Groovy if condition.
     * This can be null, which means "true".
     * Package visible for the tests only.
     *
     * @deprecated as of 2.2.0, use #getCombinationFilter() and #setCombinationFilter() instead
     */
    @Deprecated
    private volatile String combinationFilter;

    /**
     * List of active {@link Builder}s configured for this project.
     *
     * @deprecated as of 2.2.0
     *             don't use this field directly, logic was moved to {@link org.hudsonci.api.model.IProjectProperty}.
     *             Use getter/setter for accessing to this field.
     */
    @Deprecated
    private DescribableList<Builder,Descriptor<Builder>> builders =
            new DescribableList<Builder,Descriptor<Builder>>(this);

    /**
     * List of active {@link Publisher}s configured for this project.
     *
     * @deprecated as of 2.2.0
     *             don't use this field directly, logic was moved to {@link org.hudsonci.api.model.IProjectProperty}.
     *             Use getter/setter for accessing to this field.
     */
    @Deprecated
    private DescribableList<Publisher,Descriptor<Publisher>> publishers =
            new DescribableList<Publisher,Descriptor<Publisher>>(this);

    /**
     * List of active {@link BuildWrapper}s configured for this project.
     *
     * @deprecated as of 2.2.0
     *             don't use this field directly, logic was moved to {@link org.hudsonci.api.model.IProjectProperty}.
     *             Use getter/setter for accessing to this field.
     */
    @Deprecated
    private DescribableList<BuildWrapper,Descriptor<BuildWrapper>> buildWrappers =
            new DescribableList<BuildWrapper,Descriptor<BuildWrapper>>(this);

    /**
     * All {@link MatrixConfiguration}s, keyed by their {@link MatrixConfiguration#getName() names}.
     */
    private transient /*final*/ Map<Combination,MatrixConfiguration> configurations = new CopyOnWriteMap.Tree<Combination,MatrixConfiguration>();

    /**
     * @see #getActiveConfigurations()
     */
    @CopyOnWrite
    private transient /*final*/ Set<MatrixConfiguration> activeConfigurations = new LinkedHashSet<MatrixConfiguration>();

    /**
     * @deprecated as of 2.2.0, use #isRunSequentially() and #setRunSequentially() instead
     */
    @Deprecated
    private boolean runSequentially;

    /**
     * Filter to select a number of combinations to build first
     *
     * @deprecated as of 2.2.0, use #getTouchStoneCombinationFilter() and #setTouchStoneCombinationFilter() instead
     */
    @Deprecated
    private String touchStoneCombinationFilter;

    /**
     * Required result on the touchstone combinations, in order to
     * continue with the rest
     *
     * @deprecated as of 2.2.0, use #getTouchStoneResultCondition() and #setTouchStoneResultCondition() instead
     */
    @Deprecated
    private Result touchStoneResultCondition;

    /**
     * @deprecated as of 2.2.0, use #getCustomWorkspace() and #setCustomWorkspace() instead
     */
    @Deprecated
    private String customWorkspace;

    public MatrixProject(String name) {
        this(Hudson.getInstance(), name);
    }

    public MatrixProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    /**
     * @inheritDoc
     */
    public AxisList getAxes() {
        return getAxesListProjectProperty(AXES_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setAxes(AxisList axes) throws IOException {
        getAxesListProjectProperty(AXES_PROPERTY_NAME).setValue(axes);
        rebuildConfigurations();
        save();
    }

    /**
     * @inheritDoc
     */
    public boolean isRunSequentially() {
        return getBooleanProperty(RUN_SEQUENTIALLY_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setRunSequentially(boolean runSequentially) throws IOException {
        getBooleanProperty(RUN_SEQUENTIALLY_PROPERTY_NAME).setValue(runSequentially);
        save();
    }

    /**
     * @inheritDoc
     */
    public String getCombinationFilter() {
        return getStringProperty(COMBINATION_FILTER_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setCombinationFilter(String combinationFilter) throws IOException {
        getStringProperty(COMBINATION_FILTER_PROPERTY_NAME).setValue(combinationFilter);
        rebuildConfigurations();
        save();
    }

    /**
     * @inheritDoc
     */
    public String getTouchStoneCombinationFilter() {
        return getStringProperty(TOUCH_STONE_COMBINATION_FILTER_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setTouchStoneCombinationFilter(String touchStoneCombinationFilter) {
        getStringProperty(TOUCH_STONE_COMBINATION_FILTER_PROPERTY_NAME).setValue(touchStoneCombinationFilter);
    }

    /**
     * @inheritDoc
     */
    public Result getTouchStoneResultCondition() {
        return getResultProperty(TOUCH_STONE_RESULT_CONDITION_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setTouchStoneResultCondition(Result touchStoneResultCondition) {
        getResultProperty(TOUCH_STONE_RESULT_CONDITION_PROPERTY_NAME).setValue(touchStoneResultCondition);
    }

    /**
     * @inheritDoc
     */
    public String getCustomWorkspace() {
        return getStringProperty(CUSTOM_WORKSPACE_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setCustomWorkspace(String customWorkspace) throws IOException {
        getStringProperty(CUSTOM_WORKSPACE_PROPERTY_NAME).setValue(customWorkspace);
    }

    /**
     * @inheritDoc
     */
    public List<Builder> getBuilders() {
        return getBuildersList().toList();
    }

	@SuppressWarnings("unchecked")
    public DescribableList<Builder,Descriptor<Builder>> getBuildersList() {
        return getDescribableListProjectProperty(BUILDERS_PROPERTY_NAME).getValue();
    }


    public void setBuilders(DescribableList<Builder,Descriptor<Builder>> builders) {
        getDescribableListProjectProperty(BUILDERS_PROPERTY_NAME).setValue(builders);
    }

    /**
     * @inheritDoc
     */
    public Map<Descriptor<Publisher>,Publisher> getPublishers() {
        return getPublishersList().toMap();
    }

    /**
     * @inheritDoc
     */
	@SuppressWarnings("unchecked")
    public DescribableList<Publisher, Descriptor<Publisher>> getPublishersList() {
        return getDescribableListProjectProperty(PUBLISHERS_PROPERTY_NAME).getValue();
    }

    public void setPublishers(DescribableList<Publisher, Descriptor<Publisher>> publishers) {
        getDescribableListProjectProperty(PUBLISHERS_PROPERTY_NAME).setValue(publishers);
    }

    /**
     * @inheritDoc
     */
	@SuppressWarnings("unchecked")
    public DescribableList<BuildWrapper, Descriptor<BuildWrapper>> getBuildWrappersList() {
        return getDescribableListProjectProperty(BUILD_WRAPPERS_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public Map<Descriptor<BuildWrapper>,BuildWrapper> getBuildWrappers() {
        return getBuildWrappersList().toMap();
    }

    public void setBuildWrappers(DescribableList<BuildWrapper, Descriptor<BuildWrapper>> buildWrappers) {
        getDescribableListProjectProperty(BUILD_WRAPPERS_PROPERTY_NAME).setValue(buildWrappers);
    }


    @Override
    protected void buildProjectProperties() throws IOException {
        super.buildProjectProperties();
        //Convert legacy properties to IProjectProperty logic
        if (null != axes && null == getProperty(AXES_PROPERTY_NAME)) {
            //we shouldn't rebuild the axis configuration
            getAxesListProjectProperty(AXES_PROPERTY_NAME).setValue(axes);
            axes = null;//Reset to null. No longer needed.
        }
        if (null != combinationFilter && null == getProperty(COMBINATION_FILTER_PROPERTY_NAME)) {
            //we shouldn't rebuild the axis configuration
            getStringProperty(COMBINATION_FILTER_PROPERTY_NAME).setValue(combinationFilter);
            combinationFilter = null;//Reset to null. No longer needed.
        }
        if ( null == getProperty(RUN_SEQUENTIALLY_PROPERTY_NAME)) {
            getBooleanProperty(RUN_SEQUENTIALLY_PROPERTY_NAME).setValue(runSequentially);
            runSequentially = false;
        }
        if (null != touchStoneCombinationFilter && null == getProperty(TOUCH_STONE_COMBINATION_FILTER_PROPERTY_NAME)) {
            setTouchStoneCombinationFilter(touchStoneCombinationFilter);
            touchStoneCombinationFilter = null;//Reset to null. No longer needed.
        }
        if (null != touchStoneResultCondition && null == getProperty(TOUCH_STONE_RESULT_CONDITION_PROPERTY_NAME)) {
            setTouchStoneResultCondition(touchStoneResultCondition);
            touchStoneResultCondition = null;//Reset to null. No longer needed.
        }
        if (null != customWorkspace && null == getProperty(CUSTOM_WORKSPACE_PROPERTY_NAME)) {
            setCustomWorkspace(customWorkspace);
            customWorkspace = null;//Reset to null. No longer needed.
        }
        if (null == getProperty(BUILDERS_PROPERTY_NAME)) {
            setBuilders(builders);
            builders = null;
        }
        if (null == getProperty(BUILD_WRAPPERS_PROPERTY_NAME)) {
            setBuildWrappers(buildWrappers);
            buildWrappers = null;
        }
        if (null == getProperty(PUBLISHERS_PROPERTY_NAME)) {
            setPublishers(publishers);
            publishers = null;
        }
        save();
        rebuildConfigurations();
    }

    @Override
    protected List<Action> createTransientActions() {
        List<Action> r = super.createTransientActions();

        for (BuildStep step : getBuildersList())
            r.addAll(step.getProjectActions(this));
        for (BuildStep step : getPublishersList())
            r.addAll(step.getProjectActions(this));
        for (BuildWrapper step : getBuildWrappersList())
            r.addAll(step.getProjectActions(this));
        for (Trigger trigger : getTriggersList())
            r.addAll(trigger.getProjectActions());

        return r;
    }

    /**
     * Gets the subset of {@link AxisList} that are not system axes.
     *
     * @deprecated as of 1.373
     *      System vs user difference are generalized into extension point.
     */
    public List<Axis> getUserAxes() {
        List<Axis> r = new ArrayList<Axis>();
        for (Axis a : getAxes())
            if(!a.isSystem())
                r.add(a);
        return r;
    }

    public Layouter<MatrixConfiguration> getLayouter() {
        return new Layouter<MatrixConfiguration>(getAxes()) {
            protected MatrixConfiguration getT(Combination c) {
                return getItem(c);
            }
        };
    }

    @Override
    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        super.onLoad(parent,name);
        Collections.sort(getAxes()); // perhaps the file was edited on disk and the sort order might have been broken
        getBuildersList().setOwner(this);
        getPublishersList().setOwner(this);
        getBuildWrappersList().setOwner(this);
        rebuildConfigurations();
    }

    @Override
    public void logRotate() throws IOException, InterruptedException {
        super.logRotate();
        // perform the log rotation of inactive configurations to make sure
        // their logs get eventually discarded 
        for (MatrixConfiguration config : configurations.values()) {
            if(!config.isActiveConfiguration())
                config.logRotate();
        }
    }

    /**
     * Recursively search for configuration and put them to the map
     *
     * <p>
     * The directory structure would be <tt>axis-a/b/axis-c/d/axis-e/f</tt> for
     * combination [a=b,c=d,e=f]. Note that two combinations [a=b,c=d] and [a=b,c=d,e=f]
     * can both co-exist (where one is an archived record and the other is live, for example)
     * so search needs to be thorough.
     *
     * @param dir
     *      Directory to be searched.
     * @param result
     *      Receives the loaded {@link MatrixConfiguration}s.
     * @param combination
     *      Combination of key/values discovered so far while traversing the directories.
     *      Read-only.
     */
    private void loadConfigurations( File dir, CopyOnWriteMap.Tree<Combination,MatrixConfiguration> result, Map<String,String> combination ) {
        File[] axisDirs = dir.listFiles(new FileFilter() {
            public boolean accept(File child) {
                return child.isDirectory() && child.getName().startsWith("axis-");
            }
        });
        if(axisDirs==null)      return;

        for (File subdir : axisDirs) {
            String axis = subdir.getName().substring(5);    // axis name

            File[] valuesDir = subdir.listFiles(new FileFilter() {
                public boolean accept(File child) {
                    return child.isDirectory();
                }
            });
            if(valuesDir==null) continue;   // no values here

            for (File v : valuesDir) {
                Map<String,String> c = new HashMap<String, String>(combination);
                c.put(axis,TokenList.decode(v.getName()));

                try {
                    XmlFile config = Items.getConfigFile(v);
                    if(config.exists()) {
                        Combination comb = new Combination(c);
                        // if we already have this in memory, just use it.
                        // otherwise load it
                        MatrixConfiguration item=null;
                        if(this.configurations!=null)
                            item = this.configurations.get(comb);
                        if(item==null) {
                            item = (MatrixConfiguration) config.read();
                            item.setCombination(comb);
                            item.onLoad(this, v.getName());
                        }
                        result.put(item.getCombination(), item);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to load matrix configuration "+v,e);
                }
                loadConfigurations(v,result,c);
            }
        }
    }

    /**
     * Rebuilds the {@link #configurations} list and {@link #activeConfigurations}.
     */
    void rebuildConfigurations() throws IOException {
        // for the tests
        if(!isAllowSave()){
            return;
        }
        // backward compatibility check to see if there's any data in the old structure
        // if so, bring them to the newer structure.
        File[] oldDirs = getConfigurationsDir().listFiles(new FileFilter() {
            public boolean accept(File child) {
                return child.isDirectory() && !child.getName().startsWith("axis-");
            }
        });
        if (oldDirs != null) {
            // rename the old directory to the new one
            for (File dir : oldDirs) {
                try {
                    Combination c = Combination.fromString(dir.getName());
                    dir.renameTo(getRootDirFor(c));
                } catch (IllegalArgumentException e) {
                    // it's not a configuration dir. Just ignore.
                }
            }
        }

        CopyOnWriteMap.Tree<Combination,MatrixConfiguration> configurations =
            new CopyOnWriteMap.Tree<Combination,MatrixConfiguration>();
        loadConfigurations(getConfigurationsDir(),configurations,Collections.<String,String>emptyMap());
        this.configurations = configurations;

        // find all active configurations
        Set<MatrixConfiguration> active = new LinkedHashSet<MatrixConfiguration>();
        for (Combination c : getAxes().list()) {
            AxisList axes = getAxes();
            String combinationFilter = getCombinationFilter();
            if(c.evalGroovyExpression(axes,combinationFilter)) {
        		LOGGER.fine("Adding configuration: " + c);
	            MatrixConfiguration config = configurations.get(c);
	            if(config==null) {
	                config = new MatrixConfiguration(this,c);
	                config.save();
	                configurations.put(config.getCombination(), config);
	            }
	            active.add(config);
        	}
        }
        this.activeConfigurations = active;
    }

    private File getConfigurationsDir() {
        return new File(getRootDir(),"configurations");
    }

    /**
     * Gets all active configurations.
     * <p>
     * In contract, inactive configurations are those that are left for archival purpose
     * and no longer built when a new {@link MatrixBuild} is executed.
     */
    public Collection<MatrixConfiguration> getActiveConfigurations() {
        return activeConfigurations;
    }

    public Collection<MatrixConfiguration> getItems() {
        return configurations.values();
    }

    @Override
    public Collection<? extends Job> getAllJobs() {
        Set<Job> jobs = new HashSet<Job>(getItems());
        jobs.add(this);
        return jobs;
    }

    public String getUrlChildPrefix() {
        return ".";
    }

    public MatrixConfiguration getItem(String name) {
        return getItem(Combination.fromString(name));
    }

    public MatrixConfiguration getItem(Combination c) {
        return configurations.get(c);
    }

    public File getRootDirFor(MatrixConfiguration child) {
        return getRootDirFor(child.getCombination());
    }

    public void onRenamed(MatrixConfiguration item, String oldName, String newName) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void onDeleted(MatrixConfiguration item) throws IOException {
        // noop
    }

    public File getRootDirFor(Combination combination) {
        File f = getConfigurationsDir();
        for (Entry<String, String> e : combination.entrySet())
            f = new File(f,"axis-"+e.getKey()+'/'+Util.rawEncode(e.getValue()));
        f.getParentFile().mkdirs();
        return f;
    }

    /**
     * @see #getJDKs()
     */
    @Override @Deprecated
    public JDK getJDK() {
        return super.getJDK();
    }

    /**
     * Gets the {@link JDK}s where the builds will be run.
     * @return never null but can be empty
     */
    public Set<JDK> getJDKs() {
        Axis a = getAxes().find("jdk");
        if(a==null)  return Collections.emptySet();
        Set<JDK> r = new HashSet<JDK>();
        for (String j : a) {
            JDK jdk = Hudson.getInstance().getJDK(j);
            if(jdk!=null)
                r.add(jdk);
        }
        return r;
    }

    /**
     * Gets the {@link Label}s where the builds will be run.
     * @return never null
     */
    public Set<Label> getLabels() {
        Set<Label> r = new HashSet<Label>();
        for (Combination c : getAxes().subList(LabelAxis.class).list())
            r.add(Hudson.getInstance().getLabel(Util.join(c.values(),"&&")));
        return r;
    }

    public Publisher getPublisher(Descriptor<Publisher> descriptor) {
        for (Publisher p : publishers) {
            if(p.getDescriptor()==descriptor)
                return p;
        }
        return null;
    }

    protected Class<MatrixBuild> getBuildClass() {
        return MatrixBuild.class;
    }

    public boolean isFingerprintConfigured() {
        return false;
    }

    protected void buildDependencyGraph(DependencyGraph graph) {
        getPublishersList().buildDependencyGraph(this,graph);
        getBuildersList().buildDependencyGraph(this,graph);
        getBuildWrappersList().buildDependencyGraph(this,graph);
    }

    public MatrixProject asProject() {
        return this;
    }

    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        try {
            MatrixConfiguration item = getItem(token);
            if(item!=null)
            return item;
        } catch (IllegalArgumentException _) {
            // failed to parse the token as Combination. Must be something else
        }
        return super.getDynamic(token,req,rsp);
    }

    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        super.submit(req, rsp);

        JSONObject json = req.getSubmittedForm();

        setCombinationFilter(
            req.getParameter(HAS_COMBINATION_FILTER_PARAM) != null ? Util.nullify(req.getParameter(
                COMBINATION_FILTER_PROPERTY_NAME)) : null);

        if (req.getParameter(HAS_TOUCH_STONE_COMBINATION_FILTER_PARAM)!=null) {
            setTouchStoneCombinationFilter(Util.nullify(req.getParameter(TOUCH_STONE_COMBINATION_FILTER_PARAM)));
            setTouchStoneResultCondition(Result.fromString(req.getParameter(TOUCH_STONE_RESULT_CONDITION_PARAM)));
        } else {
            setTouchStoneCombinationFilter(null);
        }

        setCustomWorkspace(
            req.hasParameter(CUSTOM_WORKSPACE_PARAM) ? req.getParameter(CUSTOM_WORKSPACE_DIRECTORY_PARAM) : null);

        // parse system axes
        DescribableList<Axis, AxisDescriptor> newAxes = DescribableListUtil.buildFromHetero(this, req, json, "axis",
            Axis.all());
        checkAxisNames(newAxes);
        setAxes(new AxisList(newAxes.toList()));

        setRunSequentially(json.has(RUN_SEQUENTIALLY_PROPERTY_NAME));

        setBuildWrappers(DescribableListUtil.buildFromJson(this, req, json, BuildWrappers.getFor(this)));
        setBuilders(DescribableListUtil.buildFromHetero(this, req, json, "builder", Builder.all()));
        setPublishers(DescribableListUtil.buildFromJson(this, req, json,
            BuildStepDescriptor.filter(Publisher.all(), this.getClass())));

        rebuildConfigurations();
    }

    /**
     * Verifies that Axis names are valid and unique.
     */
    private void checkAxisNames(Iterable<Axis> newAxes) throws FormException {
        HashSet<String> axisNames = new HashSet<String>();
        for (Axis a : newAxes) {
            FormValidation fv = a.getDescriptor().doCheckName(a.getName());
            if (fv.kind!=Kind.OK)
                throw new FormException(Messages.MatrixProject_DuplicateAxisName(),fv,"axis.name");

            if (axisNames.contains(a.getName()))
                throw new FormException(Messages.MatrixProject_DuplicateAxisName(),"axis.name");
            axisNames.add(a.getName());
        }
    }

    /**
     * Also delete all the workspaces of the configuration, too.
     */
    @Override
    public HttpResponse doDoWipeOutWorkspace() throws IOException, ServletException, InterruptedException {
        HttpResponse rsp = super.doDoWipeOutWorkspace();
        for (MatrixConfiguration c : configurations.values())
            c.doDoWipeOutWorkspace();
        return rsp;
    }


    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends AbstractProjectDescriptor {
        public String getDisplayName() {
            return Messages.MatrixProject_DisplayName();
        }

        public MatrixProject newInstance(ItemGroup parent, String name) {
            return new MatrixProject(parent,name);
        }

        /**
         * All {@link AxisDescriptor}s that contribute to the UI.
         */
        public List<AxisDescriptor> getAxisDescriptors() {
            List<AxisDescriptor> r = new ArrayList<AxisDescriptor>();
            for (AxisDescriptor d : Axis.all()) {
                if (d.isInstantiable())
                    r.add(d);
            }
            return r;
        }
    }

    /**
     * For the unit tests only. Sets cascadingProject for the job.
     *
     * @param cascadingProject parent job
     */
    void setCascadingProject(MatrixProject cascadingProject) {
        this.cascadingProject = cascadingProject;
    }

    /**
     * @inheritDoc
     */
    protected void setAllowSave(Boolean allowSave) {
        super.setAllowSave(allowSave);
    }

    private static final Logger LOGGER = Logger.getLogger(MatrixProject.class.getName());
}
