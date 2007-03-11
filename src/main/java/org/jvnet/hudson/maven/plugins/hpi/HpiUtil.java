package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.artifact.Artifact;

import java.io.IOException;
import java.util.jar.JarFile;

/**
 * @author Kohsuke Kawaguchi
 */
class HpiUtil {
    static boolean isPlugin(Artifact artifact) throws IOException {
        JarFile jar = new JarFile(artifact.getFile());
        try {
            return jar.getManifest().getMainAttributes().getValue("Plugin-Class")!=null;
        } finally {
            jar.close();
        }
    }
}
