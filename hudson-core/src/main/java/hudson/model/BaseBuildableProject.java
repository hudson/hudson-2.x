/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak, Nikita Levyankov
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
package hudson.model;

import hudson.Functions;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.util.CascadingUtil;
import hudson.util.DescribableList;
import hudson.util.DescribableListUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.hudsonci.api.model.IBaseBuildableProject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base buildable project.
 *
 * @author Anton Kozak.
 */
public abstract class BaseBuildableProject<P extends BaseBuildableProject<P,B>,B extends AbstractBuild<P,B>>
    extends AbstractProject<P, B>
    implements Saveable, BuildableItemWithBuildWrappers, IBaseBuildableProject {

    public static final String BUILDERS_PROPERTY_NAME = "builders";

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
     * Creates a new project.
     * @param parent parent {@link ItemGroup}.
     * @param name the name of the project.
     */
    public BaseBuildableProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @Override
    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        super.onLoad(parent, name);

        getBuildersList().setOwner(this);
        getPublishersList().setOwner(this);
        getBuildWrappersList().setOwner(this);
    }

    @Override
    protected void buildProjectProperties() throws IOException {
        super.buildProjectProperties();
        convertBuildersProjectProperty();
        convertBuildWrappersProjectProperties();
        convertPublishersProperties();
    }

    protected void buildDependencyGraph(DependencyGraph graph) {
        getPublishersList().buildDependencyGraph(this,graph);
        getBuildersList().buildDependencyGraph(this,graph);
        getBuildWrappersList().buildDependencyGraph(this,graph);
    }

    @Override
    protected void submit( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        super.submit(req,rsp);
        JSONObject json = req.getSubmittedForm();
        buildBuildWrappers(req, json, BuildWrappers.getFor(this));
        setBuilders(DescribableListUtil.buildFromHetero(this, req, json, "builder", Builder.all()));
        buildPublishers(req, json, BuildStepDescriptor.filter(Publisher.all(), this.getClass()));
    }

    @Override
    @SuppressWarnings("unchecked")
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
     * @inheritDoc
     */
    public List<Builder> getBuilders() {
        return getBuildersList().toList();
    }

    /**
     * @inheritDoc
     */
    @SuppressWarnings("unchecked")
    public DescribableList<Builder,Descriptor<Builder>> getBuildersList() {
        return CascadingUtil.getDescribableListProjectProperty(this, BUILDERS_PROPERTY_NAME).getValue();
    }

    /**
     * @inheritDoc
     */
    public void setBuilders(DescribableList<Builder,Descriptor<Builder>> builders) {
        CascadingUtil.getDescribableListProjectProperty(this, BUILDERS_PROPERTY_NAME).setValue(builders);
    }

    /**
     * @inheritDoc
     */
    public Map<Descriptor<Publisher>,Publisher> getPublishers() {
        return getPublishersList().toMap();
    }

    public Publisher getPublisher(Descriptor<Publisher> descriptor) {
        return (Publisher) CascadingUtil.getExternalProjectProperty(this, descriptor.getJsonSafeClassName()).getValue();
    }

    /**
     * Returns the list of the publishers available in the hudson.
     *
     * @return the list of the publishers available in the hudson.
     */
    public DescribableList<Publisher, Descriptor<Publisher>> getPublishersList() {
        return DescribableListUtil.convertToDescribableList(Functions.getPublisherDescriptors(this), this);
    }

    /**
     * Adds a new {@link BuildStep} to this {@link Project} and saves the configuration.
     *
     * @param publisher publisher.
     * @throws java.io.IOException exception.
     */
    @SuppressWarnings("unchecked")
    public void addPublisher(Publisher publisher) throws IOException {
        CascadingUtil.getExternalProjectProperty(this,
            publisher.getDescriptor().getJsonSafeClassName()).setValue(publisher);
        save();
    }

    /**
     * Removes a publisher from this project, if it's active.
     *
     * @param publisher publisher.
     * @throws java.io.IOException exception.
     */
    public void removePublisher(Descriptor<Publisher> publisher) throws IOException {
        removeProjectProperty(publisher.getJsonSafeClassName());
        save();
    }

    /**
     * @inheritDoc
     */
    public Map<Descriptor<BuildWrapper>,BuildWrapper> getBuildWrappers() {
        return getBuildWrappersList().toMap();
    }

    /**
     * @inheritDoc
     */
    public DescribableList<BuildWrapper, Descriptor<BuildWrapper>> getBuildWrappersList() {
        return DescribableListUtil.convertToDescribableList(Functions.getBuildWrapperDescriptors(this), this);
    }

    /**
     * Builds publishers.
     *
     * @param req {@link StaplerRequest}
     * @param json {@link JSONObject}
     * @param descriptors list of descriptors.
     * @throws hudson.model.Descriptor.FormException if any.
     */
    protected void buildPublishers(StaplerRequest req, JSONObject json, List<Descriptor<Publisher>> descriptors)
        throws FormException {
        CascadingUtil.buildExternalProperties(req, json, descriptors, this);
    }

    /**
     * Builds BuildWrappers.
     *
     * @param req {@link StaplerRequest}
     * @param json {@link JSONObject}
     * @param descriptors list of descriptors.
     * @throws hudson.model.Descriptor.FormException if any.
     */
    protected void buildBuildWrappers(StaplerRequest req, JSONObject json, List<Descriptor<BuildWrapper>> descriptors)
        throws FormException {
        CascadingUtil.buildExternalProperties(req, json, descriptors, this);
    }

    protected void convertPublishersProperties() {
        if (null != publishers) {
            putAllProjectProperties(DescribableListUtil.convertToProjectProperties(publishers, this), false);
            publishers = null;
        }
    }

    protected void convertBuildWrappersProjectProperties() {
        if (null != buildWrappers) {
            putAllProjectProperties(DescribableListUtil.convertToProjectProperties(buildWrappers, this), false);
            buildWrappers = null;
        }
    }

    protected void convertBuildersProjectProperty() {
        if (null != builders && null == getProperty(BUILDERS_PROPERTY_NAME)) {
            setBuilders(builders);
            builders = null;
        }
    }
}
