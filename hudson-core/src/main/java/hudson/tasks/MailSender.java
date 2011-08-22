/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi,   Bruce Chapman, Daniel Dyer, Jean-Baptiste Quenot
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.CheckPoint;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Core logic of sending out notification e-mail.
 *
 * @author Jesse Glick
 * @author Kohsuke Kawaguchi
 */
public class MailSender {
    /**
     * Whitespace-separated list of e-mail addresses that represent recipients.
     */
    private String recipients;
    
    private List<AbstractProject> includeUpstreamCommitters = new ArrayList<AbstractProject>();

    /**
     * If true, only the first unstable build will be reported.
     */
    private boolean dontNotifyEveryUnstableBuild;

    /**
     * If true, individuals will receive e-mails regarding who broke the build.
     */
    private boolean sendToIndividuals;
    
    /**
     * The charset to use for the text and subject.
     */
    private String charset;


    public MailSender(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals) {
    	this(recipients, dontNotifyEveryUnstableBuild, sendToIndividuals, "UTF-8");
    }

    public MailSender(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals, String charset) {
        this(recipients,dontNotifyEveryUnstableBuild,sendToIndividuals,charset, Collections.<AbstractProject>emptyList());
    }
  
    public MailSender(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals, String charset, Collection<AbstractProject> includeUpstreamCommitters) {
        this.recipients = recipients;
        this.dontNotifyEveryUnstableBuild = dontNotifyEveryUnstableBuild;
        this.sendToIndividuals = sendToIndividuals;
        this.charset = charset;
        this.includeUpstreamCommitters.addAll(includeUpstreamCommitters);
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
            return createFailureMail(build, listener);
        }

        if (build.getResult() == Result.UNSTABLE) {
            if (!dontNotifyEveryUnstableBuild)
                return createUnstableMail(build, listener);
            Result prev = findPreviousBuildResult(build);
            if (prev == Result.SUCCESS)
                return createUnstableMail(build, listener);
        }

        if (build.getResult() == Result.SUCCESS) {
            Result prev = findPreviousBuildResult(build);
            if (prev == Result.FAILURE)
                return createBackToNormalMail(build, Messages.MailSender_BackToNormal_Normal(), listener);
            if (prev == Result.UNSTABLE)
                return createBackToNormalMail(build, Messages.MailSender_BackToNormal_Stable(), listener);
        }

        return null;
    }

    private MimeMessage createBackToNormalMail(AbstractBuild<?, ?> build, String subject, BuildListener listener) throws MessagingException {
        MimeMessage msg = createEmptyMail(build, listener);

        msg.setSubject(getSubject(build, Messages.MailSender_BackToNormalMail_Subject(subject)),charset);
        StringBuilder buf = new StringBuilder();
        appendBuildUrl(build, buf);
        msg.setText(buf.toString(),charset);

        return msg;
    }

    private MimeMessage createUnstableMail(AbstractBuild<?, ?> build, BuildListener listener) throws MessagingException {
        MimeMessage msg = createEmptyMail(build, listener);

        String subject = Messages.MailSender_UnstableMail_Subject();

        AbstractBuild<?, ?> prev = build.getPreviousBuild();
        boolean still = false;
        if(prev!=null) {
            if(prev.getResult()==Result.SUCCESS)
                subject =Messages.MailSender_UnstableMail_ToUnStable_Subject();
            else if(prev.getResult()==Result.UNSTABLE) {
                subject = Messages.MailSender_UnstableMail_StillUnstable_Subject();
                still = true;
            }
        }

        msg.setSubject(getSubject(build, subject),charset);
        StringBuilder buf = new StringBuilder();
        // Link to project changes summary for "still unstable" if this or last build has changes
        if (still && !(build.getChangeSet().isEmptySet() && prev.getChangeSet().isEmptySet()))
            appendUrl(Util.encode(build.getProject().getUrl()) + "changes", buf);
        else
            appendBuildUrl(build, buf);
        msg.setText(buf.toString(), charset);

        return msg;
    }

    private void appendBuildUrl(AbstractBuild<?, ?> build, StringBuilder buf) {
        appendUrl(Util.encode(build.getUrl())
                  + (build.getChangeSet().isEmptySet() ? "" : "changes"), buf);
    }

    private void appendUrl(String url, StringBuilder buf) {
        String baseUrl = Mailer.descriptor().getUrl();
        if (baseUrl != null)
            buf.append(Messages.MailSender_Link(baseUrl, url)).append("\n\n");
    }

    private MimeMessage createFailureMail(AbstractBuild<?, ?> build, BuildListener listener) throws MessagingException, InterruptedException {
        MimeMessage msg = createEmptyMail(build, listener);

        msg.setSubject(getSubject(build, Messages.MailSender_FailureMail_Subject()),charset);

        StringBuilder buf = new StringBuilder();
        appendBuildUrl(build, buf);

        boolean firstChange = true;
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            if (firstChange) {
                firstChange = false;
                buf.append(Messages.MailSender_FailureMail_Changes()).append("\n\n");
            }
            buf.append('[');
            buf.append(entry.getAuthor().getFullName());
            buf.append("] ");
            String m = entry.getMsg();
            if (m!=null) {
                buf.append(m);
                if (!m.endsWith("\n")) {
                    buf.append('\n');
                }
            }
            buf.append('\n');
        }

        buf.append("------------------------------------------\n");

        try {
            // Restrict max log size to avoid sending enormous logs over email.
            // Interested users can always look at the log on the web server.
            List<String> lines = build.getLog(MAX_LOG_LINES);

            String workspaceUrl = null, artifactUrl = null;
            Pattern wsPattern = null;
            String baseUrl = Mailer.descriptor().getUrl();
            if (baseUrl != null) {
                // Hyperlink local file paths to the repository workspace or build artifacts.
                // Note that it is possible for a failure mail to refer to a file using a workspace
                // URL which has already been corrected in a subsequent build. To fix, archive.
                workspaceUrl = baseUrl + Util.encode(build.getProject().getUrl()) + "ws/";
                artifactUrl = baseUrl + Util.encode(build.getUrl()) + "artifact/";
                FilePath ws = build.getWorkspace();
                // Match either file or URL patterns, i.e. either
                // c:\hudson\workdir\jobs\foo\workspace\src\Foo.java
                // file:/c:/hudson/workdir/jobs/foo/workspace/src/Foo.java
                // will be mapped to one of:
                // http://host/hudson/job/foo/ws/src/Foo.java
                // http://host/hudson/job/foo/123/artifact/src/Foo.java
                // Careful with path separator between $1 and $2:
                // workspaceDir will not normally end with one;
                // workspaceDir.toURI() will end with '/' if and only if workspaceDir.exists() at time of call
                wsPattern = Pattern.compile("(" +
                    Pattern.quote(ws.getRemote()) + "|" + Pattern.quote(ws.toURI().toString()) + ")[/\\\\]?([^:#\\s]*)");
            }
            for (String line : lines) {
                line = line.replace('\0',' '); // shall we replace other control code? This one is motivated by http://www.nabble.com/Problems-with-NULL-characters-in-generated-output-td25005177.html
                if (wsPattern != null) {
                    // Perl: $line =~ s{$rx}{$path = $2; $path =~ s!\\\\!/!g; $workspaceUrl . $path}eg;
                    Matcher m = wsPattern.matcher(line);
                    int pos = 0;
                    while (m.find(pos)) {
                        String path = m.group(2).replace(File.separatorChar, '/');
                        String linkUrl = artifactMatches(path, build) ? artifactUrl : workspaceUrl;
                        String prefix = line.substring(0, m.start()) + '<' + linkUrl + Util.encode(path) + '>';
                        pos = prefix.length();
                        line = prefix + line.substring(m.end());
                        // XXX better style to reuse Matcher and fix offsets, but more work
                        m = wsPattern.matcher(line);
                    }
                }
                buf.append(line);
                buf.append('\n');
            }
        } catch (IOException e) {
            // somehow failed to read the contents of the log
            buf.append(Messages.MailSender_FailureMail_FailedToAccessBuildLog()).append("\n\n").append(Functions.printThrowable(e));
        }

        msg.setText(buf.toString(),charset);

        return msg;
    }

    private MimeMessage createEmptyMail(AbstractBuild<?, ?> build, BuildListener listener) throws MessagingException {
        MimeMessage msg = new HudsonMimeMessage(Mailer.descriptor().createSession());
        // TODO: I'd like to put the URL to the page in here,
        // but how do I obtain that?
        msg.setContent("", "text/plain");
        msg.setFrom(new InternetAddress(Mailer.descriptor().getAdminAddress()));
        msg.setSentDate(new Date());

        Set<InternetAddress> rcp = new LinkedHashSet<InternetAddress>();
        StringTokenizer tokens = new StringTokenizer(recipients);
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

        for (AbstractProject project : includeUpstreamCommitters) {
            includeCulpritsOf(project, build, listener, rcp);
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

    private String getSubject(AbstractBuild<?, ?> build, String caption) {
        return caption + ' ' + build.getFullDisplayName();
    }

    /**
     * Check whether a path (/-separated) will be archived.
     */
    protected boolean artifactMatches(String path, AbstractBuild<?, ?> build) {
        return false;
    }

    public static boolean debug = false;

    private static final int MAX_LOG_LINES = Integer.getInteger(MailSender.class.getName()+".maxLogLines",250);


    /**
     * Sometimes the outcome of the previous build affects the e-mail we send, hence this checkpoint.
     */
    private static final CheckPoint CHECKPOINT = new CheckPoint("mail sent");
}
