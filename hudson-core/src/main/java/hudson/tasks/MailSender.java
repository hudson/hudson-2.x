/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi,
 * Bruce Chapman, Daniel Dyer, Jean-Baptiste Quenot, Anton Kozak
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.tasks;

import hudson.mail.BaseMailSender;
import hudson.tasks.mail.impl.BackToNormalBuildMail;
import hudson.tasks.mail.impl.FailureBuildMail;
import hudson.tasks.mail.impl.UnstableBuildMail;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.CheckPoint;
import hudson.model.Result;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Core logic of sending out notification e-mail.
 *
 * @author Jesse Glick
 * @author Kohsuke Kawaguchi
 */
public class MailSender extends BaseMailSender {

    private List<AbstractProject> upstreamProjects = new ArrayList<AbstractProject>();

    /**
     * If true, only the first unstable build will be reported.
     */
    private boolean dontNotifyEveryUnstableBuild;

    /**
     * If true, individuals will receive e-mails regarding who broke the build.
     */
    private boolean sendToIndividuals;


    public MailSender(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals) {
    	this(recipients, dontNotifyEveryUnstableBuild, sendToIndividuals, DEFAULT_CHARSET);
    }

    public MailSender(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals,
                      String charset) {
        this(recipients, dontNotifyEveryUnstableBuild, sendToIndividuals, charset,
            Collections.<AbstractProject>emptyList());
    }

    public MailSender(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals,
                      String charset, Collection<AbstractProject> includeUpstreamCommitters) {
        super(recipients, charset);
        this.dontNotifyEveryUnstableBuild = dontNotifyEveryUnstableBuild;
        this.sendToIndividuals = sendToIndividuals;
        this.upstreamProjects.addAll(includeUpstreamCommitters);
    }

    public boolean execute(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException {
        try {
            MimeMessage mail = getMail(build, listener);
            if (mail != null) {
                // if the previous e-mail was sent for a success, this new e-mail
                // is not a follow up
                AbstractBuild<?, ?> pb = build.getPreviousBuild();
                if(pb!=null && pb.getResult()==Result.SUCCESS) {
                    mail.removeHeader("In-Reply-To");
                    mail.removeHeader("References");
                }

                Address[] allRecipients = mail.getAllRecipients();
                if (allRecipients != null) {
                    StringBuilder buf = new StringBuilder("Sending e-mails to:");
                    for (Address a : allRecipients)
                        buf.append(' ').append(a);
                    listener.getLogger().println(buf);
                    Mailer.descriptor().send((HudsonMimeMessage) mail);

                    build.addAction(new MailMessageIdAction(mail.getMessageID()));
                } else {
                    listener.getLogger().println(Messages.MailSender_ListEmpty());
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        } finally {
            CHECKPOINT.report();
        }

        return true;
    }

    /**
     * To correctly compute the state change from the previous build to this build,
     * we need to ignore aborted builds.
     * See http://www.nabble.com/Losing-build-state-after-aborts--td24335949.html
     *
     * <p>
     * And since we are consulting the earlier result, we need to wait for the previous build
     * to pass the check point.
     */
    private Result findPreviousBuildResult(AbstractBuild<?,?> b) throws InterruptedException {
        CHECKPOINT.block();
        do {
            b=b.getPreviousBuild();
            if(b==null) return null;
        } while(b.getResult()==Result.ABORTED);
        return b.getResult();
    }

    protected MimeMessage getMail(AbstractBuild<?, ?> build, BuildListener listener) throws MessagingException, InterruptedException {
        if (build.getResult() == Result.FAILURE) {
            return new FailureBuildMail(getRecipients(), sendToIndividuals, upstreamProjects, getCharset()).
                getMail(build, listener);
        }

        if (build.getResult() == Result.UNSTABLE) {
            if (!dontNotifyEveryUnstableBuild)
                return new UnstableBuildMail(getRecipients(), sendToIndividuals, upstreamProjects, getCharset()).
                    getMail(build, listener);
            Result prev = findPreviousBuildResult(build);
            if (prev == Result.SUCCESS)
                return new UnstableBuildMail(getRecipients(), sendToIndividuals, upstreamProjects, getCharset()).
                    getMail(build, listener);
        }

        if (build.getResult() == Result.SUCCESS) {
            Result prev = findPreviousBuildResult(build);
            if (prev == Result.FAILURE)
                return new BackToNormalBuildMail(getRecipients(), sendToIndividuals, upstreamProjects, getCharset(),
                    Messages.MailSender_BackToNormal_Normal()).getMail(build, listener);
            if (prev == Result.UNSTABLE)
                return new BackToNormalBuildMail(getRecipients(), sendToIndividuals, upstreamProjects, getCharset(),
                    Messages.MailSender_BackToNormal_Stable()).getMail(build, listener);
        }

        return null;
    }

    /**
     * Check whether a path (/-separated) will be archived.
     */
    //TODO investigate where it's used.
    protected boolean artifactMatches(String path, AbstractBuild<?, ?> build) {
        return false;
    }

    /**
     * Sometimes the outcome of the previous build affects the e-mail we send, hence this checkpoint.
     */
    private static final CheckPoint CHECKPOINT = new CheckPoint("mail sent");
}
