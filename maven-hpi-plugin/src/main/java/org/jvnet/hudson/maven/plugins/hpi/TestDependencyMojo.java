package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.Set;

/**
 * Places test-dependency plugins into somewhere the test harness can pick up.
 *
 * <p>
 * See {@code TestPluginManager.loadBundledPlugins()} where the test harness uses it.
 *
 * @goal resolve-test-dependencies
 * @requiresDependencyResolution test
 * @author Kohsuke Kawaguchi
 */
public class TestDependencyMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File testDir = new File(project.getBuild().getTestOutputDirectory(),"test-dependencies");
        testDir.mkdirs();

        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(new File(testDir,"index")),"UTF-8");

            for (Artifact a : (Set<Artifact>) project.getArtifacts()) {
                if(!HpiUtil.isPlugin(a))
                    continue;

                getLog().debug("Copying "+a.getId()+" as a test dependency");
                File dst = new File(testDir, a.getArtifactId() + ".hpi");
                FileUtils.copyFile(a.getFile(),dst);
                w.write(a.getArtifactId()+"\n");
            }

            w.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy dependency plugins",e);
        }
    }
}
