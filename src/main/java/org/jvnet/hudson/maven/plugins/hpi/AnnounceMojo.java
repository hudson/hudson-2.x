package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.ProcessingException;

/**
 * Posts a release announcement.
 *
 * @goal announce
 * @author Kohsuke Kawaguchi
 */
public class AnnounceMojo extends AbstractJavaNetMojo {
    /**
     * Body of the release announcement. Optional.
     *
     * @parameter
     */
    private String contents;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if(contents==null && project.getUrl()!=null)
                contents = String.format("<a href='%s'>see webpage for more details</a>",project.getUrl());

            JNProject p = connect().getProject("hudson");
            p.getNewsItems().createNewsItem(
                null,
                project.getName()+' '+project.getVersion()+" released",
                contents,
                null,
                project.getUrl()
            );
        } catch (ProcessingException e) {
            throw new MojoExecutionException("Failed to post announcement",e);
        }
    }
}
