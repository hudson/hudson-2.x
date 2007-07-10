package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.JNFileFolder;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.FileStatus;
import org.kohsuke.jnt.ProcessingException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.Set;

/**
 * Posts the hpi file to java.net.
 *
 * @goal upload
 * @execute phase=package
 * @requiresDependencyResolution compile
 * @author Kohsuke Kawaguchi
 */
public class UploadMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter expression="${userName}"
     */
    private String userName;

    /**
     * @parameter expression="${password}"
     */
    private String password;


    public void execute() throws MojoExecutionException, MojoFailureException {
        Artifact artifact = project.getArtifact();
        if(artifact==null)
            throw new MojoExecutionException("Project has no artifact, whereas an .hpi artifact was expected");
        File archive = artifact.getFile();
        if(archive==null || !archive.exists())
            throw new MojoExecutionException("Project has no artifact, whereas an .hpi artifact was expected");

        if(!project.getPackaging().equals("hpi")) {
            getLog().warn("Skipping hpi:upload because this is not an hpi project");
            return;
        }

        try {
            JavaNet jn;

            if(userName!=null && password!=null)
                jn = JavaNet.connect(userName,password);
            else {
                getLog().debug("Loading "+new File(new File(System.getProperty("user.home")),".java.net"));
                jn = JavaNet.connect();
            }
            JNProject p = jn.getProject("hudson");
            JNFileFolder f = p.getFolder("/plugins/" + project.getArtifactId());
            if(f==null) {
                getLog().info("Creating folder");
                f = p.getFolder("/plugins");
                f = f.createFolder(project.getArtifactId(),project.getDescription());
            }

            String date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(new Date());
            f.uploadFile(project.getVersion(),date+" release. Built with Hudson "+getHudsonVersion(),
                project.getVersion().endsWith("SNAPSHOT") ? FileStatus.DRAFT : FileStatus.STABLE,
                archive);
        } catch (ProcessingException e) {
            throw new MojoExecutionException("Failed to upload the artifact",e);
        }
    }

    private String getHudsonVersion() {
        for(Artifact a : (Set<Artifact>)project.getArtifacts()) {
            if(a.getArtifactId().equals("hudson-core"))
                return a.getVersion();
        }
        return "UNKNOWN";
    }
}
