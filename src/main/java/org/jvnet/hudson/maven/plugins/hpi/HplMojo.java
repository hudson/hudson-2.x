package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Section;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Generate .hpl file.
 *
 * @author Kohsuke Kawaguchi
 * @goal hpl
 * @requiresDependencyResolution runtime
 */
public class HplMojo extends AbstractHpiMojo {
    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     *
     * @parameter expression="${hudsonHome}
     */
    private File hudsonHome;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if(hudsonHome==null) {
            throw new MojoExecutionException(
                "Property hudsonHome needs to be set to $HUDSON_HOME. Please use 'mvn -DhudsonHome=...' or" +
                "put <settings><profiles><profile><properties><property><hudsonHome>...</...>"
            );
        }

        File hplFile = new File(hudsonHome, "plugins/" + project.getBuild().getFinalName() + ".hpl");
        getLog().info("Generating "+hplFile);

        PrintWriter printWriter = null;
        try {
            Manifest mf = new Manifest();
            Section mainSection = mf.getMainSection();
            setAttributes(mainSection);

            // compute Libraries entry
            StringBuffer buf = new StringBuffer();
            buf.append(new File(project.getBuild().getOutputDirectory()).getAbsoluteFile());
            for (Artifact a : (Set<Artifact>) project.getArtifacts()) {
                buf.append(',').append(a.getFile());
            }
            mainSection.addAttributeAndCheck(new Attribute("Libraries",buf.toString()));

            // compute Resource-Path entry
            mainSection.addAttributeAndCheck(new Attribute("Resource-Path",warSourceDirectory.getAbsolutePath()));

            printWriter = new PrintWriter(new FileWriter(hplFile));
            mf.write(printWriter);
        } catch (ManifestException e) {
            throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
        } finally {
            IOUtil.close(printWriter);
        }
    }
}
