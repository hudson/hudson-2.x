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

import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Mailer;
import hudson.tasks.Messages;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Class used for the mail preparation if build was broken.
 */
public class FailureBuildMail extends BaseBuildResultMail {

    public FailureBuildMail(String recipients, boolean sendToIndividuals,
                            List<AbstractProject> upstreamProjects, String charset) {
        super(recipients, sendToIndividuals, upstreamProjects, charset);
    }

    /**
     * @inheritDoc
     */
    public MimeMessage getMail(AbstractBuild<?, ?> build, BuildListener listener)
        throws MessagingException, InterruptedException {
        MimeMessage msg = createEmptyMail(build, listener);

        msg.setSubject(getSubject(build, Messages.MailSender_FailureMail_Subject()), getCharset());

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
            if (m != null) {
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
                    Pattern.quote(ws.getRemote()) + "|" + Pattern.quote(ws.toURI().toString())
                    + ")[/\\\\]?([^:#\\s]*)");
            }
            for (String line : lines) {
                line = line.replace('\0',
                    ' '); // shall we replace other control code? This one is motivated by http://www.nabble.com/Problems-with-NULL-characters-in-generated-output-td25005177.html
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
            buf.append(Messages.MailSender_FailureMail_FailedToAccessBuildLog()).append("\n\n").append(
                Functions.printThrowable(e));
        }
        appendFooter(buf);
        msg.setText(buf.toString(), getCharset());

        return msg;
    }

    /**
     * Check whether a path (/-separated) will be archived.
     */
    public boolean artifactMatches(String path, AbstractBuild<?, ?> build) {
        ArtifactArchiver aa = build.getProject().getPublishersList().get(ArtifactArchiver.class);
        if (aa == null) {
            //LOGGER.finer("No ArtifactArchiver found");
            return false;
        }
        String artifacts = aa.getArtifacts();
        for (String include : artifacts.split("[, ]+")) {
            String pattern = include.replace(File.separatorChar, '/');
            if (pattern.endsWith("/")) {
                pattern += "**";
            }
            if (SelectorUtils.matchPath(pattern, path)) {
                //LOGGER.log(Level.FINER, "DescriptorImpl.artifactMatches true for {0} against {1}",
                //    new Object[]{path, pattern});
                return true;
            }
        }
        //LOGGER.log(Level.FINER, "DescriptorImpl.artifactMatches for {0} matched none of {1}",
        //    new Object[]{path, artifacts});
        return false;
    }

}
