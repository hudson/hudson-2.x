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
*    Kohsuke Kawaguchi, Bruce Chapman
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin.reporters;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.tasks.MailSender;
import hudson.tasks.Mailer;

import org.eclipse.hudson.legacy.maven.plugin.MavenBuild;
import org.eclipse.hudson.legacy.maven.plugin.MavenReporter;
import org.eclipse.hudson.legacy.maven.plugin.MavenReporterDescriptor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

import net.sf.json.JSONObject;

/**
 * Sends out an e-mail notification for Maven build result.
 * @author Kohsuke Kawaguchi
 */
public class MavenMailer extends MavenReporter {
    /**
     * @see Mailer
     */
    public String recipients;
    public boolean dontNotifyEveryUnstableBuild;
    public boolean sendToIndividuals;

    public boolean end(MavenBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        new MailSender(recipients,dontNotifyEveryUnstableBuild,sendToIndividuals).execute(build,listener);
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends MavenReporterDescriptor {
        public String getDisplayName() {
            return Messages.MavenMailer_DisplayName();
        }

        public String getHelpFile() {
            return "/help/project-config/mailer.html";
        }

        // reuse the config from the mailer.
        @Override
        public String getConfigPage() {
            return getViewPage(Mailer.class,"config.jelly");
        }

        public MavenReporter newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            MavenMailer m = new MavenMailer();
            req.bindParameters(m,"mailer_");
            m.dontNotifyEveryUnstableBuild = req.getParameter("mailer_notifyEveryUnstableBuild")==null;
            return m;
        }
    }

    private static final long serialVersionUID = 1L;
}
