package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.artifact.Artifact;
import org.kohsuke.stapler.framework.io.IOException2;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.Arrays;

import sun.tools.jar.resources.jar;

/**
 * @author Kohsuke Kawaguchi
 */
class HpiUtil {
    static boolean isPlugin(Artifact artifact) throws IOException {
        try {
            // some artifacts aren't even Java, so ignore those.
            if(!artifact.getType().equals("jar"))    return false;
            JarFile jar = new JarFile(artifact.getFile());
            try {
                Manifest manifest = jar.getManifest();
                if(manifest==null)  return false;
                for( String key : Arrays.asList("Plugin-Class","Plugin-Version")) {
                    if(manifest.getMainAttributes().getValue(key) != null)
                        return true;
                }
                return false;
            } finally {
                jar.close();
            }
        } catch (IOException e) {
            throw new IOException2("Failed to open artifact "+artifact.toString()+" at "+artifact.getFile(),e);
        }
    }
}
