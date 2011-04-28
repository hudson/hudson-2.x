package org.jvnet.hudson.maven.plugins.hpi;

import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.kohsuke.stapler.framework.io.IOException2;

/**
 * @author Kohsuke Kawaguchi
 */
class HpiUtil {

    private static final String HUDSON_CORE_GROUP_ID = "org.jvnet.hudson.main";
    private static final String HUDSON_CORE_ARTIFACT_ID = "hudson-core";

    private static final String[] HUDSON_PLUGIN_HEADERS = {"Plugin-Class","Plugin-Version"};

    static boolean isPlugin(Artifact artifact) throws IOException {
        try {
            // some artifacts aren't even Java, so ignore those.
            if(!artifact.getType().equals("jar"))    return false;

            // this can happened with maven 3 and doesn't have any side effect here
            if(artifact.getFile() == null ) return false;
            // could a reactor member in member (mvn test-compile with core :-) )
            if(artifact.getFile().isDirectory()) return false;
            
            JarFile jar = new JarFile(artifact.getFile());
            try {
                Manifest manifest = jar.getManifest();
                if(manifest==null)  return false;
                for( String key : HUDSON_PLUGIN_HEADERS ) {
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

    static String findHudsonVersion(Collection<Artifact> artifacts, Log log) {
        for(Artifact a :artifacts) {
            if(HUDSON_CORE_GROUP_ID.equals(a.getGroupId()) &&
                    HUDSON_CORE_ARTIFACT_ID.equals(a.getArtifactId())) {
                log.info("Targeting Hudson-Version: "+a.getVersion());
                return a.getVersion();
            }
        }
        log.warn("Cannot determine Hudson-Version from project dependencies");
        return null;
    }
}
