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
import hudson.tasks.mail.BuildResultMail;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.tasks.HudsonMimeMessage;
import hudson.tasks.MailMessageIdAction;
import hudson.tasks.MailSender;
import hudson.tasks.Mailer;
import hudson.tasks.Messages;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.collections.CollectionUtils;

/**
 * Base class for all project build result mails.
 */
public abstract class BaseBuildResultMail implements BuildResultMail {

    protected static final int MAX_LOG_LINES = Integer.getInteger(MailSender.class.getName()+".maxLogLines",250);

    //TODO where it's used?
    public static boolean debug = false;

    /**
     * Whitespace-separated list of e-mail addresses that represent recipients.
     */
    private String recipients;

    /**
     * The charset to use for the text and subject.
     */
    private String charset;

    /**
     * The list of upstream projects.
     */
    private List<AbstractProject> upstreamProjects;

    /**
     * If true, individuals will receive e-mails regarding who broke the build.
     */
    private boolean sendToIndividuals;


    public BaseBuildResultMail(String recipients, boolean sendToIndividuals, List<AbstractProject> upstreamProjects,
                               String charset) {
        this.recipients = recipients;
        this.sendToIndividuals = sendToIndividuals;
        this.upstreamProjects = upstreamProjects;
        this.charset = charset;
    }

    /**
     * Returns recipients.
     *
     * @return recipients.
     */
    public String getRecipients() {
        return recipients;
    }

    /**
     * Returns charset.
     *
     * @return charset.
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Returns prefix for subject of automatically generated emails.
     *
     * @return prefix for subject.
     */
    protected String getSubjectPrefix() {
        return hudson.mail.Messages.hudson_email_subject_prefix();
    }
    /**
     * Creates empty mail.
     *
     * @param build build.
     * @param listener listener.
     * @return empty mail.
     * @throws MessagingException exception if any.
     */
    protected MimeMessage createEmptyMail(AbstractBuild<?, ?> build, BuildListener listener) throws MessagingException {
        MimeMessage msg = new HudsonMimeMessage(Mailer.descriptor().createSession());
        // TODO: I'd like to put the URL to the page in here,
        // but how do I obtain that?
        msg.setContent("", "text/plain");
        msg.setFrom(new InternetAddress(Mailer.descriptor().getAdminAddress()));
        msg.setSentDate(new Date());

        Set<InternetAddress> rcp = new LinkedHashSet<InternetAddress>();
        StringTokenizer tokens = new StringTokenizer(getRecipients());
        while (tokens.hasMoreTokens()) {
            String address = tokens.nextToken();
            if(address.startsWith("upstream-individuals:")) {
                // people who made a change in the upstream
                String projectName = address.substring("upstream-individuals:".length());
                AbstractProject up = Hudson.getInstance().getItemByFullName(projectName,AbstractProject.class);
                if(up==null) {
                    listener.getLogger().println("No such project exist: "+projectName);
                    continue;
                }
                includeCulpritsOf(up, build, listener, rcp);
            } else {
                // ordinary address
                try {
                    rcp.add(new InternetAddress(address));
                } catch (AddressException e) {
                    // report bad address, but try to send to other addresses
                    e.printStackTrace(listener.error(e.getMessage()));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(upstreamProjects)) {
            for (AbstractProject project : upstreamProjects) {
                includeCulpritsOf(project, build, listener, rcp);
            }
        }

        if (sendToIndividuals) {
            Set<User> culprits = build.getCulprits();

            if(debug)
                listener.getLogger().println("Trying to send e-mails to individuals who broke the build. sizeof(culprits)=="+culprits.size());

            rcp.addAll(buildCulpritList(listener,culprits));
        }
        msg.setRecipients(Message.RecipientType.TO, rcp.toArray(new InternetAddress[rcp.size()]));

        AbstractBuild<?, ?> pb = build.getPreviousBuild();
        if(pb!=null) {
            MailMessageIdAction b = pb.getAction(MailMessageIdAction.class);
            if(b!=null) {
                msg.setHeader("In-Reply-To",b.messageId);
                msg.setHeader("References",b.messageId);
            }
        }

        return msg;
    }

    /**
     * Appends build URL to the builder.
     *
     * @param build build.
     * @param buf {@link StringBuilder}.
     */
    protected void appendBuildUrl(AbstractBuild<?, ?> build, StringBuilder buf) {
        appendUrl(Util.encode(build.getUrl())
                  + (build.getChangeSet().isEmptySet() ? "" : "changes"), buf);
    }

    /**
     * Appends URL to the builder.
     *
     * @param url url.
     * @param buf {@link StringBuilder}.
     */
    protected void appendUrl(String url, StringBuilder buf) {
        String baseUrl = Mailer.descriptor().getUrl();
        if (baseUrl != null)
            buf.append(Messages.MailSender_Link(baseUrl, url)).append("\n\n");
    }

    /**
     * Appends footer to the mail builder.
     *
     * @param buf {@link StringBuilder}.
     */
    protected void appendFooter(StringBuilder buf) {
        String footer = getTextFooter();
        if (footer != null) {
            buf.append(footer);
        }
    }


    /**
     * Returns the subject of the mail.
     *
     * @param build build.
     * @param caption the caption.
     * @return prepared subject.
     */
    protected String getSubject(AbstractBuild<?, ?> build, String caption) {
        return new StringBuilder().append(getSubjectPrefix())
            .append(" ")
            .append(caption)
            .append(" ")
            .append(build.getFullDisplayName())
            .toString();
    }

    private void includeCulpritsOf(AbstractProject upstreamProject, AbstractBuild<?, ?> currentBuild, BuildListener listener, Set<InternetAddress> recipientList) throws AddressException {
        AbstractBuild<?,?> upstreamBuild = currentBuild.getUpstreamRelationshipBuild(upstreamProject);
        AbstractBuild<?,?> previousBuild = currentBuild.getPreviousBuild();
        AbstractBuild<?,?> previousBuildUpstreamBuild = previousBuild!=null ? previousBuild.getUpstreamRelationshipBuild(upstreamProject) : null;
        if(previousBuild==null && upstreamBuild==null && previousBuildUpstreamBuild==null) {
            listener.getLogger().println("Unable to compute the changesets in "+ upstreamProject +". Is the fingerprint configured?");
            return;
        }
        if(previousBuild==null || upstreamBuild==null || previousBuildUpstreamBuild==null) {
            listener.getLogger().println("Unable to compute the changesets in "+ upstreamProject);
            return;
        }
        AbstractBuild<?,?> b=previousBuildUpstreamBuild;
        do {
            recipientList.addAll(buildCulpritList(listener,b.getCulprits()));
            b = b.getNextBuild();
        } while ( b != upstreamBuild && b != null );
    }


    private Set<InternetAddress> buildCulpritList(BuildListener listener, Set<User> culprits) throws AddressException {
        Set<InternetAddress> r = new HashSet<InternetAddress>();
        for (User a : culprits) {
            String adrs = Util.fixEmpty(a.getProperty(Mailer.UserProperty.class).getAddress());
            if(debug)
                listener.getLogger().println("  User "+a.getId()+" -> "+adrs);
            if (adrs != null)
                r.add(new InternetAddress(adrs));
            else {
                listener.getLogger().println(Messages.MailSender_NoAddress(a.getFullName()));
            }
        }
        return r;
    }

    /**
     * Returns text footer for all automatically generated emails.
     *
     * @return text footer.
     */
    private String getTextFooter() {
        return hudson.mail.Messages.hudson_email_footer();
    }

}
