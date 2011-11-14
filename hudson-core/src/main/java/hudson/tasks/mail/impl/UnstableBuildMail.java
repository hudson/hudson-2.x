/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak
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
package hudson.tasks.mail.impl;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.Messages;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Class used for the mail preparation if build is unstable.
 */
public class UnstableBuildMail extends BaseBuildResultMail {

    public UnstableBuildMail(String recipients, boolean sendToIndividuals,
                             List<AbstractProject> upstreamProjects, String charset) {
        super(recipients, sendToIndividuals, upstreamProjects, charset);
    }

    /**
     * @inheritDoc
     */
    public MimeMessage getMail(AbstractBuild<?, ?> build, BuildListener listener)
        throws MessagingException, InterruptedException {
        MimeMessage msg = createEmptyMail(build, listener);

        String subject = Messages.MailSender_UnstableMail_Subject();

        AbstractBuild<?, ?> prev = build.getPreviousBuild();
        boolean still = false;
        if (prev != null) {
            if (prev.getResult() == Result.SUCCESS) {
                subject = Messages.MailSender_UnstableMail_ToUnStable_Subject();
            } else if (prev.getResult() == Result.UNSTABLE) {
                subject = Messages.MailSender_UnstableMail_StillUnstable_Subject();
                still = true;
            }
        }

        msg.setSubject(getSubject(build, subject), getCharset());
        StringBuilder buf = new StringBuilder();
        // Link to project changes summary for "still unstable" if this or last build has changes
        if (still && !(build.getChangeSet().isEmptySet() && prev.getChangeSet().isEmptySet())) {
            appendUrl(Util.encode(build.getProject().getUrl()) + "changes", buf);
        } else {
            appendBuildUrl(build, buf);
        }
        appendFooter(buf);
        msg.setText(buf.toString(), getCharset());

        return msg;
    }
}
