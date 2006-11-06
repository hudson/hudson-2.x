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

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.archiver.jar.Manifest.Section;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generate a manifest for a Hudson plugin.
 *
 * @goal manifest
 * @phase process-resources
 * @requiresDependencyResolution runtime
 */
public class HpiManifestMojo extends AbstractHpiMojo {
    ///**
    // * The Jar archiver.
    // *
    // * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
    // * @required
    // */
    //private WarArchiver warArchiver;

    /**
     * The maven archive configuration to use.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Executes the WarMojo on the current project.
     *
     * @throws MojoExecutionException if an error occured while building the webapp
     */
    public void execute() throws MojoExecutionException {
        File manifestFile = new File(outputDirectory, "MANIFEST.MF");
        // since this involves scanning the source code, don't do it unless necessary
        if(manifestFile.exists())
            return;

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
