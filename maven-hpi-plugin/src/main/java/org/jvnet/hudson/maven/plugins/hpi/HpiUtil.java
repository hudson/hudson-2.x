package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.framework.io.IOException2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Kohsuke Kawaguchi
 */
class HpiUtil {
    private static final String HUDSON_CORE_GROUP_NAME = "org.jvnet.hudson.main";
    private static final String HUDSON_CORE_ARTIFACT_ID = "hudson-core";
    private static final String JAR_PACKAGE_NAME = "jar";
    private static final String PLUGIN_CLASS_KEY = "Plugin-Class";
    private static final String PLUGIN_VERSION_KEY = "Plugin-Version";

    static boolean isPlugin(Artifact artifact) throws IOException {
        try {
            // some artifacts aren't even Java, so ignore those.
            if (!artifact.getType().equals(JAR_PACKAGE_NAME)) return false;

            // this can happened with maven 3 and doesn't have any side effect here
            if (artifact.getFile() == null) return false;
            // could a reactor member in member (mvn test-compile with core :-) )
            if (artifact.getFile().isDirectory()) return false;

            JarFile jar = new JarFile(artifact.getFile());
            try {
                Manifest manifest = jar.getManifest();
                if (manifest == null) return false;
                for (String key : Arrays.asList(PLUGIN_CLASS_KEY, PLUGIN_VERSION_KEY)) {
                    if (manifest.getMainAttributes().getValue(key) != null)
                        return true;
                }
                return false;
            } finally {
                jar.close();
            }
        } catch (IOException e) {
            throw new IOException2("Failed to open artifact " + artifact.toString() + " at " + artifact.getFile(), e);
        }
    }

    //TODO why we cannot use maven-hpi-plugin version?
    static String findHudsonVersion(MavenProject project) {
        for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            if (artifact.getGroupId().equals(HUDSON_CORE_GROUP_NAME) &&
                    artifact.getArtifactId().equals(HUDSON_CORE_ARTIFACT_ID)) {
                return artifact.getVersion();
            }
        }
        for (Dependency dependency : (List<Dependency>) project.getDependencies()) {
            if (dependency.getGroupId().equals(HUDSON_CORE_GROUP_NAME) &&
                    dependency.getArtifactId().equals(HUDSON_CORE_ARTIFACT_ID)) {
                return dependency.getVersion();
            }
        }
        return null;
    }
}
