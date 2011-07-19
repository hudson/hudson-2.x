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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.model.FreeStyleProject;
import hudson.tasks.Mailer.DescriptorImpl;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.Email;
import org.jvnet.mock_javamail.Mailbox;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

/**
 * @author Kohsuke Kawaguchi
 */
public class MailerTest extends HudsonTestCase {
    @Bug(1566)
    public void testSenderAddress() throws Exception {
        // intentionally give the whole thin in a double quote
        Mailer.descriptor().setAdminAddress("\"me <me@sun.com>\"");

        String recipient = "you <you@sun.com>";
        Mailbox yourInbox = Mailbox.get(new InternetAddress(recipient));
        yourInbox.clear();

        // create a project to simulate a build failure
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new FailureBuilder());
        Mailer m = new Mailer();
        m.recipients = recipient;
        project.getPublishersList().add(m);

        project.scheduleBuild2(0).get();

        assertEquals(1,yourInbox.size());
        Address[] senders = yourInbox.get(0).getFrom();
        assertEquals(1,senders.length);
        assertEquals("me <me@sun.com>",senders[0].toString());
    }

    /**
     * Makes sure the use of "localhost" in the Hudson URL reports a warning.
     */
    public void testLocalHostWarning() throws Exception {
        HtmlPage p = new WebClient().goTo("configure");
        HtmlInput url = p.getFormByName("config").getInputByName("_.url");
        url.setValueAttribute("http://localhost:1234/");
        assertTrue(p.getDocumentElement().getTextContent().contains("instead of localhost"));
    }

    @Email("http://www.nabble.com/email-recipients-disappear-from-freestyle-job-config-on-save-to25479293.html")
    public void testConfigRoundtrip() throws Exception {
        Mailer m = new Mailer();
        m.recipients = "kk@kohsuke.org";
        m.dontNotifyEveryUnstableBuild = true;
        m.sendToIndividuals = true;
        verifyRoundtrip(m);

        m = new Mailer();
        m.recipients = "";
        m.dontNotifyEveryUnstableBuild = false;
        m.sendToIndividuals = false;
        verifyRoundtrip(m);
    }

    private void verifyRoundtrip(Mailer m) throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.getPublishersList().add(m);
        submit(new WebClient().getPage(p,"configure").getFormByName("config"));
        assertEqualBeans(m,p.getPublishersList().get(Mailer.class),"recipients,dontNotifyEveryUnstableBuild,sendToIndividuals");
    }

    public void testGlobalConfigRoundtrip() throws Exception {
        DescriptorImpl d = Mailer.descriptor();
        d.setAdminAddress("admin@me");
        d.setDefaultSuffix("default-suffix");
        d.setHudsonUrl("http://nowhere/");
        d.setSmtpHost("smtp.host");
        d.setSmtpPort("1025");
        d.setUseSsl(true);
        d.setSmtpAuth("user","pass");

        submit(new WebClient().goTo("configure").getFormByName("config"));

        assertEquals("admin@me",d.getAdminAddress());
        assertEquals("default-suffix",d.getDefaultSuffix());
        assertEquals("http://nowhere/",d.getUrl());
        assertEquals("smtp.host",d.getSmtpServer());
        assertEquals("1025",d.getSmtpPort());
        assertEquals(true,d.getUseSsl());
        assertEquals("user",d.getSmtpAuthUserName());
        assertEquals("pass",d.getSmtpAuthPassword());

        d.setUseSsl(false);
        d.setSmtpAuth(null,null);
        submit(new WebClient().goTo("configure").getFormByName("config"));
        assertEquals(false,d.getUseSsl());
        assertNull("expected null, got: " + d.getSmtpAuthUserName(), d.getSmtpAuthUserName());
        assertNull("expected null, got: " + d.getSmtpAuthPassword(), d.getSmtpAuthPassword());
    }
}
