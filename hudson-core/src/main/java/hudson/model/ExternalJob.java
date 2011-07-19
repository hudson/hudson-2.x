/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi, id:cactusman
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.model.RunMap.Constructor;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Job that runs outside Hudson whose result is submitted to Hudson
 * (either via web interface, or simply by placing files on the file system,
 * for compatibility.)
 *
 * @author Kohsuke Kawaguchi
 */
public class ExternalJob extends ViewJob<ExternalJob,ExternalRun> implements TopLevelItem {
    public ExternalJob(String name) {
        this(Hudson.getInstance(),name);
    }

    public ExternalJob(ItemGroup parent, String name) {
        super(parent,name);
    }

    @Override
    protected void reload() {
        this.runs.load(this,new Constructor<ExternalRun>() {
            public ExternalRun create(File dir) throws IOException {
                return new ExternalRun(ExternalJob.this,dir);
            }
        });
    }


    // keep track of the previous time we started a build
    private transient long lastBuildStartTime;

    /**
     * Creates a new build of this project for immediate execution.
     *
     * Needs to be synchronized so that two {@link #newBuild()} invocations serialize each other.
     */
    public synchronized ExternalRun newBuild() throws IOException {
        // make sure we don't start two builds in the same second
        // so the build directories will be different too
        long timeSinceLast = System.currentTimeMillis() - lastBuildStartTime;
        if (timeSinceLast < 1000) {
            try {
                Thread.sleep(1000 - timeSinceLast);
            } catch (InterruptedException e) {
            }
        }
        lastBuildStartTime = System.currentTimeMillis();

        ExternalRun run = new ExternalRun(this);
        runs.put(run);
        return run;
    }

    /**
     * Used to check if this is an external job and ready to accept a build result.
     */
    public void doAcceptBuildResult(StaplerResponse rsp) throws IOException, ServletException {
        rsp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Used to post the build result from a remote machine.
     */
    public void doPostBuildResult( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        checkPermission(AbstractProject.BUILD);
        ExternalRun run = newBuild();
        run.acceptRemoteSubmission(req.getReader());
        rsp.setStatus(HttpServletResponse.SC_OK);
    }

    public TopLevelItemDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final TopLevelItemDescriptor DESCRIPTOR = new DescriptorImpl();

    @Override
    public String getPronoun() {
        return Messages.ExternalJob_Pronoun();
    }

    public static final class DescriptorImpl extends TopLevelItemDescriptor {
        public String getDisplayName() {
            return Messages.ExternalJob_DisplayName();
        }

        public ExternalJob newInstance(ItemGroup parent, String name) {
            return new ExternalJob(parent,name);
        }
    }
}
