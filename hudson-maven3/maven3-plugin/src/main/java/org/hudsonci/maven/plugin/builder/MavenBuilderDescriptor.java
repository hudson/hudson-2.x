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

package org.hudsonci.maven.plugin.builder;

import org.hudsonci.maven.model.PropertiesDTO;
import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.maven.model.config.ChecksumModeDTO;
import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.config.DocumentTypeDTO;
import org.hudsonci.maven.model.config.FailModeDTO;
import org.hudsonci.maven.model.config.MakeModeDTO;
import org.hudsonci.maven.model.config.SnapshotUpdateModeDTO;
import org.hudsonci.maven.model.config.VerbosityDTO;
import org.hudsonci.service.SecurityService;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.RenderableEnum;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

import org.hudsonci.maven.plugin.builder.internal.BuildConfigurationExtractor;
import org.hudsonci.maven.plugin.documents.DocumentManager;
import org.hudsonci.maven.plugin.install.MavenInstallation;
import org.kohsuke.stapler.StaplerRequest;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.maven.model.ModelUtil.renderList;
import static org.hudsonci.maven.model.ModelUtil.renderProperties;
import static org.hudsonci.maven.model.config.DocumentTypeDTO.SETTINGS;
import static org.hudsonci.maven.model.config.DocumentTypeDTO.TOOLCHAINS;

/**
 * {@link MavenBuilder} descriptor.
 * 
 * Contains configuration information about the builder.  When adding new
 * options update {@link #DEFAULTS} and {@link #createConfiguration(JSONObject)}
 * with the default and UI translator.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
@Typed(Descriptor.class)
@XStreamAlias("maven-builder-descriptor")
public class MavenBuilderDescriptor
    extends BuildStepDescriptor<Builder>
{
    public static final BuildConfigurationDTO DEFAULTS = new BuildConfigurationDTO()
        .withGoals("clean install")
        .withPomFile("pom.xml")
        .withPrivateRepository(true)
        .withPrivateTmpdir(false) // may cause problems with paths with spaces, so leave off by default
        .withOffline(false)
        .withRecursive(true)
        .withErrors(false)
        .withVerbosity(VerbosityDTO.NORMAL)
        .withChecksumMode(ChecksumModeDTO.NORMAL)
        .withFailMode(FailModeDTO.NORMAL)
        .withMakeMode(MakeModeDTO.NONE)
        .withSnapshotUpdateMode(SnapshotUpdateModeDTO.NORMAL);

    public static final String DESCRIPTOR_FILE_NAME = "maven-builder-descriptor.xml";

    @XStreamOmitField
    private final SecurityService security;

    @XStreamOmitField
    private final DocumentManager documents;

    @XStreamOmitField
    private final MavenInstallation.DescriptorImpl installationDescriptor;

    private BuildConfigurationDTO defaults = DEFAULTS;

    @Inject
    public MavenBuilderDescriptor(final SecurityService security,
                                  final DocumentManager documents,
                                  final MavenInstallation.DescriptorImpl installationDescriptor)
    {
        super(MavenBuilder.class);
        this.security = checkNotNull(security);
        this.documents = checkNotNull(documents);
        this.installationDescriptor = checkNotNull(installationDescriptor);
        load();
    }

    @Override
    public String getDisplayName() {
        return "Invoke Maven 3";
    }

    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> type) {
        return true;
    }

    @Override
    public XmlFile getConfigFile() {
        return new XmlFile(new File(Hudson.getInstance().getRootDir(), DESCRIPTOR_FILE_NAME));
    }

    @JellyAccessible
    public BuildConfigurationDTO getDefaults() {
        return defaults;
    }

    public DocumentManager getDocuments() {
        return documents;
    }

    public void setDefaults(final BuildConfigurationDTO defaults) {
        this.defaults = checkNotNull(defaults);
        save();
    }

    @JellyAccessible
    public RenderableEnum[] getVerbosityValues() {
        return RenderableEnum.forEnum(VerbosityDTO.class);
    }

    @JellyAccessible
    public RenderableEnum[] getChecksumModeValues() {
        return RenderableEnum.forEnum(ChecksumModeDTO.class);
    }

    @JellyAccessible
    public RenderableEnum[] getFailModeValues() {
        return RenderableEnum.forEnum(FailModeDTO.class);
    }

    @JellyAccessible
    public RenderableEnum[] getMakeModeValues() {
        return RenderableEnum.forEnum(MakeModeDTO.class);
    }

    @JellyAccessible
    public RenderableEnum[] getSnapshotUpdateModeValues() {
        return RenderableEnum.forEnum(SnapshotUpdateModeDTO.class);
    }

    @JellyAccessible
    public Collection<DocumentDTO> getSettingsDocuments() {
        return getDocuments(SETTINGS);
    }

    @JellyAccessible
    public Collection<DocumentDTO> getToolChainsDocuments() {
        return getDocuments(TOOLCHAINS);
    }

    private Collection<DocumentDTO> getDocuments(final DocumentTypeDTO type) {
        checkNotNull(type);
        // Need to run as SYSTEM when fetching the documents to be used
        return security.callAs2(ACL.SYSTEM, new Callable<Collection<DocumentDTO>>()
        {
            public Collection<DocumentDTO> call() {
                return documents.getDocuments(type, true);
            }
        });
    }

    /**
     * Determine if a value is selected.  If the value equals the configured value or the configured
     * value is null and value equals the default value, then it is selected.
     *
     * @param value         The value, from an enum items or something; never null.
     * @param configValue   The instance configuration value; may be null.
     * @param defaultValue  The descriptor default value; may be null.
     * @return              True if the value is selected.
     */
    @JellyAccessible
    public boolean isSelected(final Object value, final Object configValue, final Object defaultValue) {
        assert value != null;
        return value.equals(configValue) || (configValue == null && value.equals(defaultValue));
    }

    /**
     * Determine the value to be used.  If the given value is non-null then it will be used, else the default value will be used.
     * For use when jelly tag does not expose a default attribute, only a value attribute.
     */
    @JellyAccessible
    public Object valueOf(final Object value, final Object defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Determine the value to be used for the given properties and render.
     */
    @JellyAccessible
    public String valueOf(final PropertiesDTO value, final PropertiesDTO defaultValue) {
        return renderProperties(value != null ? value : defaultValue);
    }

    /**
     * Determine the value to be used for the given lists and render.
     */
    @JellyAccessible
    public String valueOf(final List<String> value, final List<String> defaultValue) {
        return renderList(value != null ? value : defaultValue);
    }

    @JellyAccessible
    public MavenInstallation[] getInstallations() {
        return checkNotNull(installationDescriptor).getInstallations();
    }

    /**
     * Configure the defaults.
     */
    @Override
    public boolean configure(final StaplerRequest req, final JSONObject data) throws FormException {
        setDefaults(createConfiguration(data));
        return true;
    }

    /**
     * Create a new instance.
     */
    @Override
    public Builder newInstance(final StaplerRequest req, final JSONObject data) throws FormException {
        return new MavenBuilder(createConfiguration(data));
    }

    /**
     * Create a configuration object from the given JSON data.
     */
    private BuildConfigurationDTO createConfiguration(final JSONObject data) {
        return new BuildConfigurationExtractor(data).extract();
    }
}
