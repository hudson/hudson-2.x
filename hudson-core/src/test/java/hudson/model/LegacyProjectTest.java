/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov, Anton Kozak
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

import hudson.tasks.Mailer;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.CopyOnWriteList;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.hudsonci.model.project.property.CopyOnWriteListProjectProperty;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Test for legacy
 * <p/>
 * Date: 9/23/11
 *
 * @author Nikita Levyankov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Hudson.class, SCMTrigger.DescriptorImpl.class, TimerTrigger.DescriptorImpl.class,
    Mailer.DescriptorImpl.class, JUnitResultArchiver.DescriptorImpl.class})
public class LegacyProjectTest {

    private File config;

    @Before
    public void setUp() throws URISyntaxException {
        config = new File(FreeStyleProject.class.getResource("/hudson/model/freestyle").toURI());
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether CustomWorkspace is configured based
     * on legacy value.
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyCustomWorkspaceProperty() throws Exception {
        FreeStyleProject project = (FreeStyleProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
        project.convertCustomWorkspaceProperty();
        assertNotNull(project.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether Builders
     * from Project are configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyBuildersProperty() throws Exception {
        Project project = (Project) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        //Property should be null, because of legacy implementation. Version < 2.2.0
        assertNull(project.getProperty(Project.BUILDERS_PROPERTY_NAME));
        project.convertBuildersProjectProperty();
        //Verify builders
        assertNotNull(project.getProperty(Project.BUILDERS_PROPERTY_NAME));
        assertFalse(project.getBuildersList().isEmpty());
        assertEquals(1, project.getBuildersList().size());
        assertTrue(project.getBuildersList().get(0) instanceof Maven);
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether BuildWrappers
     * from Project are configured
     *
     * @throws Exception if any.
     */
    @Test
    @Ignore
    //TODO re-implement this method according to new implementation. Logic is similar to testConvertPublishersProperty
    public void testConvertLegacyBuildWrappersProperty() throws Exception {
        Project project = (Project) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        //Property should be null, because of legacy implementation. Version < 2.2.0
        project.convertBuildWrappersProjectProperties();
        //Verify buildWrappers
        assertTrue(project.getBuildWrappersList().isEmpty());
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether publishers
     * from Project are configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertPublishersProperty() throws Exception {
        Project project = (Project) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();

        Hudson hudson = createMock(Hudson.class);

        String mailerKey = "hudson-tasks-Mailer";
        Descriptor<Publisher> mailerDescriptor = createMock(Mailer.DescriptorImpl.class);
        expect(mailerDescriptor.getJsonSafeClassName()).andReturn(mailerKey);
        expect(hudson.getDescriptorOrDie(Mailer.class)).andReturn(mailerDescriptor);

        String jUnitKey = "hudson-task-JUnitResultArchiver";
        Descriptor<Publisher> junitDescriptor = createMock(JUnitResultArchiver.DescriptorImpl.class);
        expect(junitDescriptor.getJsonSafeClassName()).andReturn(jUnitKey);
        expect(hudson.getDescriptorOrDie(JUnitResultArchiver.class)).andReturn(junitDescriptor);

        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();

        //Publishers should be null, because of legacy implementation. Version < 2.2.0
        assertNull(project.getProperty(mailerKey));
        assertNull(project.getProperty(jUnitKey));
        project.convertPublishersProperties();
        //Verify publishers
        assertNotNull(project.getProperty(mailerKey).getValue());
        assertNotNull(project.getProperty(jUnitKey).getValue());
        verifyAll();
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyBlockBuildWhenDownstreamBuildingProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME));
        project.convertBlockBuildWhenDownstreamBuildingProperty();
        assertNotNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyBlockBuildWhenUpstreamBuildingProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME));
        project.convertBlockBuildWhenUpstreamBuildingProperty();
        assertNotNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyConcurrentBuildProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME));
        project.convertConcurrentBuildProperty();
        assertNotNull(project.getProperty(AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyCleanWorkspaceRequiredProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME));
        project.convertCleanWorkspaceRequiredProperty();
        assertNotNull(project.getProperty(AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyQuietPeriodProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.QUIET_PERIOD_PROPERTY_NAME));
        project.convertQuietPeriodProperty();
        assertNotNull(project.getProperty(AbstractProject.QUIET_PERIOD_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyScmCheckoutRetryCountProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.SCM_CHECKOUT_RETRY_COUNT_PROPERTY_NAME));
        project.convertScmCheckoutRetryCountProperty();
        assertNotNull(project.getProperty(AbstractProject.SCM_CHECKOUT_RETRY_COUNT_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
     * from AbstractProject is configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyJDKProperty() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.JDK_PROPERTY_NAME));
        project.convertJDKProperty();
        assertNotNull(project.getProperty(AbstractProject.JDK_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether LogRotator
     * from Job are configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyLogRotatorProperty() throws Exception {
        Job project = (Job) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(Job.LOG_ROTATOR_PROPERTY_NAME));
        project.convertLogRotatorProperty();
        assertNotNull(project.getProperty(Job.LOG_ROTATOR_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalling FreeStyleProject configuration and checks whether Triggers properties
     * from Job are configured.
     *
     * @throws Exception if any.
     */
    @Test
    public void testConvertLegacyTriggers() throws Exception {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        Hudson hudson = createMock(Hudson.class);

        String scmTriggerPropertyKey = "hudson-trigger-SCMTrigger";
        TriggerDescriptor scmTriggerDescriptor = createMock(SCMTrigger.DescriptorImpl.class);
        expect(scmTriggerDescriptor.getJsonSafeClassName()).andReturn(scmTriggerPropertyKey);
        expect(hudson.getDescriptorOrDie(SCMTrigger.class)).andReturn(scmTriggerDescriptor);

        String timerTriggerPropertyKey = "hudson-trigger-TimerTrigger";
        TriggerDescriptor timerTriggerDescriptor = createMock(TimerTrigger.DescriptorImpl.class);
        expect(timerTriggerDescriptor.getJsonSafeClassName()).andReturn(timerTriggerPropertyKey);
        expect(hudson.getDescriptorOrDie(TimerTrigger.class)).andReturn(timerTriggerDescriptor);

        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();

        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(scmTriggerPropertyKey));
        assertNull(project.getProperty(timerTriggerPropertyKey));
        project.convertTriggerProperties();

        assertNotNull(project.getProperty(scmTriggerPropertyKey));
        Trigger trigger = (Trigger) project.getProperty(scmTriggerPropertyKey).getValue();
        assertEquals("5 * * * *", trigger.getSpec());

        assertNotNull(project.getProperty(timerTriggerPropertyKey));
        trigger = (Trigger) project.getProperty(timerTriggerPropertyKey).getValue();
        assertEquals("*/10 * * * *", trigger.getSpec());
        verifyAll();
    }

    /**
     * Tests unmarshalling FreeStyleProject configuration and checks whether assignedNode and advancedAffinityChooser
     * properties from AbstractProject are configured.
     *
     * @throws java.io.IOException if any
     */
    @Test
    public void testConvertLegacyNodeProperties() throws IOException {
        AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.APPOINTED_NODE_PROPERTY_NAME));
        project.convertAppointedNode();
        assertNotNull(project.getProperty(AbstractProject.APPOINTED_NODE_PROPERTY_NAME));
        assertTrue(project.isAdvancedAffinityChooser());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertLegacyParameterDefinitionProperties() throws IOException {
        Job project = (Job) Items.getConfigFile(config).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(Job.PARAMETERS_DEFINITION_JOB_PROPERTY_PROPERTY_NAME));
        project.convertJobProperties();
        //Properties should be initialized
        assertNotNull(project.properties);
        CopyOnWriteListProjectProperty property = (CopyOnWriteListProjectProperty) project.getProperty(
            Job.PARAMETERS_DEFINITION_JOB_PROPERTY_PROPERTY_NAME);
        assertNotNull(property);
        CopyOnWriteList<ParametersDefinitionProperty> propertyValue = property.getValue();
        assertFalse(propertyValue.isEmpty());
        assertEquals(1, propertyValue.size());
        ParametersDefinitionProperty pdp = propertyValue.get(0);
        List<ParameterDefinition> parameterDefinitions = pdp.getParameterDefinitions();
        assertNotNull(parameterDefinitions);
        assertFalse(parameterDefinitions.isEmpty());
        String parameterName1 = "test1";
        String parameterName2 = "test2";
        assertTrue(pdp.getParameterDefinitionNames().contains(parameterName1));
        assertTrue(pdp.getParameterDefinitionNames().contains(parameterName2));
        ParameterDefinition parameterDefinition = pdp.getParameterDefinition(parameterName1);
        assertNotNull(parameterDefinition);
        assertTrue(parameterDefinition instanceof StringParameterDefinition);
        assertEquals(parameterName1, parameterDefinition.getName());
        assertEquals("df1", ((StringParameterDefinition) parameterDefinition).getDefaultValue());
        assertEquals("string value description", parameterDefinition.getDescription());

        parameterDefinition = pdp.getParameterDefinition(parameterName2);
        assertNotNull(parameterDefinition);
        assertTrue(parameterDefinition instanceof BooleanParameterDefinition);
        assertEquals(parameterName2, parameterDefinition.getName());
        assertEquals(true, ((BooleanParameterDefinition) parameterDefinition).isDefaultValue());
        assertEquals("boolean value description", parameterDefinition.getDescription());
    }
}
