package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.archiver.jar.Manifest.Section;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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
     * Path to <tt>$HUDSON_HOME</tt>. A .hpl file will be generated to this location.
     *
     * @parameter expression="${hudsonHome}
     */
    private File hudsonHome;

    public void setHudsonHome(File hudsonHome) {
        this.hudsonHome = hudsonHome;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if(!project.getPackaging().equals("hpi")) {
            getLog().info("Skipping "+project.getName()+" because it's not <packaging>hpi</packaging>");
            return;
        }

        File hplFile = computeHplFile();
        getLog().info("Generating "+hplFile);

        PrintWriter printWriter = null;
        try {
            Manifest mf = new Manifest();
            Section mainSection = mf.getMainSection();
            setAttributes(mainSection);

            // compute Libraries entry
            StringBuffer buf = new StringBuffer();

            // we want resources to be picked up before target/classes,
            // so that the original (not in the copy) will be picked up first.
            for (Resource r : (List<Resource>) project.getBuild().getResources()) {
                if(buf.length()>0)
                    buf.append(',');
                if(new File(project.getBasedir(),r.getDirectory()).exists())
                    buf.append(r.getDirectory());
            }
            if(buf.length()>0)
                buf.append(',');
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

    /**
     * Determine where to produce the .hpl file.
     */
    protected File computeHplFile() throws MojoExecutionException {
        if(hudsonHome==null) {
            throw new MojoExecutionException(
                "Property hudsonHome needs to be set to $HUDSON_HOME. Please use 'mvn -DhudsonHome=...' or" +
                "put <settings><profiles><profile><properties><property><hudsonHome>...</...>"
            );
        }

        File hplFile = new File(hudsonHome, "plugins/" + project.getBuild().getFinalName() + ".hpl");
        return hplFile;
    }
}
