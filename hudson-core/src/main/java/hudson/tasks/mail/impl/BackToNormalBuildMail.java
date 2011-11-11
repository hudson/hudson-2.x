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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.Messages;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Class used for the mail preparation if build was returned to normal state.
 */
public class BackToNormalBuildMail extends BaseBuildResultMail {

    /**
     * Current state.
     */
    private String currentState;

    public BackToNormalBuildMail(String recipients, boolean sendToIndividuals,
                                 List<AbstractProject> upstreamProjects, String charset, String currentState) {
        super(recipients, sendToIndividuals, upstreamProjects, charset);
        this.currentState = currentState;
    }

    /**
     * @inheritDoc
     */
    public MimeMessage getMail(AbstractBuild<?, ?> build, BuildListener listener)
        throws MessagingException, InterruptedException {
        MimeMessage msg = createEmptyMail(build, listener);
        msg.setSubject(getSubject(build, Messages.MailSender_BackToNormalMail_Subject(currentState)), getCharset());
        StringBuilder buf = new StringBuilder();
        appendBuildUrl(build, buf);
        appendFooter(buf);
        msg.setText(buf.toString(), getCharset());
        return msg;
    }
}
