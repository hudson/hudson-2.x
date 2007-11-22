package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractJavaNetMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     */
    protected MavenProject project;

    /**
     * @parameter expression="${userName}"
     */
    protected String userName;

    /**
     * @parameter expression="${password}"
     */
    protected String password;

    protected final JavaNet connect() throws ProcessingException {
        JavaNet jn;

        if(userName!=null && password!=null)
            jn = JavaNet.connect(userName,password);
        else {
            getLog().debug("Loading "+new File(new File(System.getProperty("user.home")),".java.net"));
            jn = JavaNet.connect();
        }
        return jn;
    }
}
