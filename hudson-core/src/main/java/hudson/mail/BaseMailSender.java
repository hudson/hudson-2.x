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
package hudson.mail;

import hudson.tasks.HudsonMimeMessage;
import hudson.tasks.Mailer;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Base logic of sending out notification e-mail.
 */
public abstract class BaseMailSender {
    private static final String DEFAULT_TEXT = "Should be overridden";

    protected static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * Whitespace-separated list of e-mail addresses that represent recipients.
     */
    private String recipients;

    /**
     * The charset to use for the text and subject.
     */
    private String charset;


    public BaseMailSender(String recipients) {
        this(recipients, DEFAULT_CHARSET);
    }

    public BaseMailSender(String recipients, String charset) {
        this.recipients = recipients;
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

    public boolean execute() {
        try {
            MimeMessage mail = getMail();
            if (mail != null) {
                if (mail.getAllRecipients() != null) {
                    Mailer.descriptor().send((HudsonMimeMessage) mail);
                }
            }
        } catch (MessagingException ignore) {
            //TODO add logging
        }
        return true;
    }

    /**
     * Returns prepared email.
     *
     * @return prepared email.
     * @throws MessagingException exception if any.
     */
    protected MimeMessage getMail() throws MessagingException {
        MimeMessage msg = new HudsonMimeMessage(Mailer.descriptor().createSession());
        msg.setContent("", "text/plain");
        msg.setFrom(new InternetAddress(Mailer.descriptor().getAdminAddress()));
        msg.setSentDate(new Date());

        Set<InternetAddress> rcp = new LinkedHashSet<InternetAddress>();
        StringTokenizer tokens = new StringTokenizer(recipients);
        while (tokens.hasMoreTokens()) {
            String address = tokens.nextToken();
            try {
                rcp.add(new InternetAddress(address));
            } catch (AddressException ignore) {
                // ignore bad address, but try to send to other addresses
            }
        }
        msg.setRecipients(Message.RecipientType.TO, rcp.toArray(new InternetAddress[rcp.size()]));
        msg.setSubject(new StringBuilder().append(getSubjectPrefix()).append(" ").append(getSubject()).toString(),
            charset);
        msg.setText(new StringBuilder().append(getText()).append(getTextFooter()).toString(), charset);
        return msg;
    }

    /**
     * Returns the text of the email.
     *
     * @return the text of the email.
     */
    protected String getText() {
        return DEFAULT_TEXT;
    }

    /**
     * Returns the subject of the email.
     *
     * @return the subject of the email.
     */
    protected String getSubject() {
        return DEFAULT_TEXT;
    }

    /**
     * Returns text footer for all automatically generated emails.
     *
     * @return text footer.
     */
    protected String getTextFooter() {
        return hudson.mail.Messages.hudson_email_footer();
    }

    /**
     * Returns prefix for subject of automatically generated emails.
     *
     * @return prefix for subject.
     */
    protected String getSubjectPrefix() {
        return hudson.mail.Messages.hudson_email_subject_prefix();
    }

}
