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
     * Tests unmarshalls FreeStyleProject configuration and checks whether CustomWorkspace property is set,
     *
     * @throws Exception if any.
     */
    @Test
    public void testLoadFreeStyleProject() throws Exception {
        File freeStyleProjectConfig = new File(FreeStyleProject.class.getResource("/hudson/model/freestyle").toURI());
        FreeStyleProject freeStyleProject = (FreeStyleProject) Items.getConfigFile(freeStyleProjectConfig).read();
        freeStyleProject.setAllowSave(false);
        freeStyleProject.initProjectProperties();
        assertNull(freeStyleProject.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
        freeStyleProject.buildProjectProperties();
        assertNotNull(freeStyleProject.getCustomWorkspace());
    }
}
