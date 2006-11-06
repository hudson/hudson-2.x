package org.jvnet.hudson.maven.plugins.hpi;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Section;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.JavaDocBuilder;

/**
 * Build a war/webapp.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: HpiMojo.java 991 2006-11-06 01:46:28Z kohsuke $
 * @goal hpi
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class HpiMojo extends AbstractHpiMojo {

    /**
     * The name of the generated hpi.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String hpiName;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     *
     * @parameter
     */
    private String classifier;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     */
    private JarArchiver jarArchiver;

    /**
     * The maven archive configuration to use.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Whether this is the main artifact being built. Set to <code>false</code> if you don't want to install or
     * deploy it to the local repository instead of the default one in an execution.
     *
     * @parameter expression="${primaryArtifact}" default-value="true"
     */
    private boolean primaryArtifact;

    // ----------------------------------------------------------------------
    // Implementation
    // ----------------------------------------------------------------------

    /**
     * Overload this to produce a test-war, for example.
     */
    protected String getClassifier() {
        return classifier;
    }

    protected static File getHpiFile(File basedir, String finalName, String classifier) {
        if (classifier == null) {
            classifier = "";
        } else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }

        return new File(basedir, finalName + classifier + ".hpi");
    }

    /**
     * Executes the WarMojo on the current project.
     *
     * @throws MojoExecutionException if an error occured while building the webapp
     */
    public void execute()
        throws MojoExecutionException {
        File hpiFile = getHpiFile(new File(outputDirectory), hpiName, classifier);

        try {
            performPackaging(hpiFile);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Error assembling hpi: " + e.getMessage(), e);
        } catch (ManifestException e) {
            throw new MojoExecutionException("Error assembling hpi", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error assembling hpi", e);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Error assembling hpi: " + e.getMessage(), e);
        }
    }

    /**
     * Generates the webapp according to the <tt>mode</tt> attribute.
     *
     * @param hpiFile the target war file
     * @throws IOException
     * @throws ArchiverException
     * @throws ManifestException
     * @throws DependencyResolutionRequiredException
     *
     */
    private void performPackaging(File hpiFile)
        throws IOException, ArchiverException, ManifestException, DependencyResolutionRequiredException,
        MojoExecutionException {
        buildExplodedWebapp(getWebappDirectory());

        //generate war file
        getLog().info("Generating hpi " + hpiFile.getAbsolutePath());

        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver(jarArchiver);

        archiver.setOutputFile(hpiFile);

        File manifestFile = new File(getWebappDirectory(), "META-INF/MANIFEST.MF");
        generateManifest(manifestFile);

        jarArchiver.addDirectory(getWebappDirectory(), getIncludes(), getExcludes());

        // create archive
        archiver.createArchive(project, archive);

        String classifier = this.classifier;
        if (classifier != null) {
            projectHelper.attachArtifact(project, "hpi", classifier, hpiFile);
        } else {
            Artifact artifact = project.getArtifact();
            if (primaryArtifact) {
                artifact.setFile(hpiFile);
            } else if (artifact.getFile() == null || artifact.getFile().isDirectory()) {
                artifact.setFile(hpiFile);
            }
        }
    }

    /**
     * Generates a manifest file to be included in the .hpi file
     */
    private void generateManifest(File manifestFile) throws MojoExecutionException {
        // since this involves scanning the source code, don't do it unless necessary
        if(manifestFile.exists())
            return;

        // create directory if it doesn't exist yet
        if(!manifestFile.getParentFile().exists())
            manifestFile.getParentFile().mkdirs();

        getLog().info("Generating "+manifestFile);

        MavenArchiver ma = new MavenArchiver();
        ma.setOutputFile(manifestFile);

        JavaClass javaClass = findPluginClass();
        if(javaClass==null)
            throw new MojoExecutionException("Unable to find a plugin class");



        PrintWriter printWriter = null;
        try {
            Manifest mf = ma.getManifest(project, archive.getManifest());
            Section mainSection = mf.getMainSection();
            mainSection.addAttributeAndCheck(new Attribute("Plugin-Class",
                javaClass.getPackage()+"."+javaClass.getName()));
            mainSection.addAttributeAndCheck(new Attribute("Long-Name",pluginName));

            printWriter = new PrintWriter(new FileWriter(manifestFile));
            mf.write(printWriter);
        } catch (ManifestException e) {
            throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
        } finally {
            IOUtil.close(printWriter);
        }
    }

    /**
     * Find a class that has "@plugin" marker.
     */
    private JavaClass findPluginClass() {
        JavaDocBuilder builder = new JavaDocBuilder();
        for (Object o : project.getCompileSourceRoots())
            builder.addSourceTree(new File((String) o));

        // look for a class that extends Plugin
        for( JavaSource js : builder.getSources() ) {
            JavaClass jc = js.getClasses()[0];
            if(jc.getTagByName("plugin")!=null)
                return jc;
        }
        return null;
    }
}
