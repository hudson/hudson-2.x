package hudson.model;

import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Test for legacy
 * <p/>
 * Date: 9/23/11
 *
 * @author Nikita Levyankov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Hudson.class})
public class LegacyProjectTest {

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether properties are configured based
     * on legacy values,
     *
     * @throws Exception if any.
     */
    @Test
    public void testLoadLegacyFreeStyleProject() throws Exception {
        File freeStyleProjectConfig = new File(FreeStyleProject.class.getResource("/hudson/model/freestyle").toURI());
        FreeStyleProject project = (FreeStyleProject) Items.getConfigFile(freeStyleProjectConfig).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        //Checks customWorkspace value
        assertNull(project.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
        project.buildProjectProperties();
        assertNotNull(project.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
    }

    /**
     * Tests unmarshalls FreeStyleProject configuration and checks whether properties
     * from AbstractProject are configured
     *
     * @throws Exception if any.
     */
    @Test
    public void testLoadLegacyAbstractProject() throws Exception {
        File freeStyleProjectConfig = new File(FreeStyleProject.class.getResource("/hudson/model/freestyle").toURI());
        AbstractProject project = (AbstractProject) Items.getConfigFile(freeStyleProjectConfig).read();
        project.setAllowSave(false);
        project.initProjectProperties();
        assertNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME));
        assertNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME));
        assertNull(project.getProperty(AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME));
        assertNull(project.getProperty(AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME));
        assertNull(project.getProperty(AbstractProject.QUIET_PERIOD_PROPERTY_NAME));
        assertNull(project.getProperty(AbstractProject.SCM_CHECKOUT_RETRY_COUNT_PROPERTY_NAME));
        project.buildProjectProperties();
        assertNotNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME));
        assertNotNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME));
        assertNotNull(project.getProperty(AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME));
        assertNotNull(project.getProperty(AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME));
        assertNotNull(project.getProperty(AbstractProject.QUIET_PERIOD_PROPERTY_NAME));
        assertNotNull(project.getProperty(AbstractProject.SCM_CHECKOUT_RETRY_COUNT_PROPERTY_NAME));
    }
}
