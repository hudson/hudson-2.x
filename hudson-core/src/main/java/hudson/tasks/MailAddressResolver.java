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
*    Kohsuke Kawaguchi, Luca Domenico Milanesio
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.scm.SCM;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Infers e-mail addresses for the user when none is specified.
 *
 * <p>
 * This is an extension point of Hudson. Plugins tha contribute new implementation
 * of this class should put {@link Extension} on your implementation class, like this:
 *
 * <pre>
 * &#64;Extension
 * class MyMailAddressResolver extends {@link MailAddressResolver} {
 *   ...
 * }
 * </pre>
 *
 * <h2>Techniques</h2>
 * <p>
 * User identity in Hudson is global, and not specific to a particular job. As a result, mail address resolution
 * only receives {@link User}, which by itself doesn't really have that much information in it.
 *
 * <p>
 * So the common technique for a mail address resolution is to define your own {@link UserProperty} types and
 * add it to {@link User} objects where more context is available. For example, an {@link SCM} implementation
 * can have a lot more information about a particular user during a check out, so that would be a good place
 * to capture information as {@link UserProperty}, which then later used by a {@link MailAddressResolver}. 
 *
 * @author Kohsuke Kawaguchi
 * @since 1.192
 */
public abstract class MailAddressResolver implements ExtensionPoint {
    /**
     * Infers e-mail address of the given user.
     *
     * <p>
     * This method is called when a {@link User} without explicitly configured e-mail
     * address is used, as an attempt to infer e-mail address.
     *
     * <p>
     * The normal strategy is to look at {@link User#getProjects() the projects that the user
     * is participating}, then use the repository information to infer the e-mail address.
     *
     * <p>
     * When multiple resolvers are installed, they are consulted in order and
     * the search will be over when an address is inferred by someone.
     *
     * <p>
     * Since {@link MailAddressResolver} is singleton, this method can be invoked concurrently
     * from multiple threads.
     *
     * @return
     *      null if the inference failed.
     */
    public abstract String findMailAddressFor(User u);
    
    public static String resolve(User u) {
        LOGGER.fine("Resolving e-mail address for \""+u+"\" ID="+u.getId());

        for (MailAddressResolver r : all()) {
            String email = r.findMailAddressFor(u);
            if(email!=null) {
                LOGGER.fine(r+" resolved "+u.getId()+" to "+email);
                return email;
            }
        }

        // fall back logic
        String extractedAddress = extractAddressFromId(u.getFullName());
        if (extractedAddress != null)
            return extractedAddress;

        if(u.getFullName().contains("@"))
            // this already looks like an e-mail ID
            return u.getFullName();

        String ds = Mailer.descriptor().getDefaultSuffix();
        if(ds!=null) {
            // another common pattern is "DOMAIN\person" in Windows. Only
            // do this when this full name is not manually set. see HUDSON-5164
            Matcher m = WINDOWS_DOMAIN_REGEXP.matcher(u.getFullName());
            if (m.matches() && u.getFullName().replace('\\','_').equals(u.getId()))
                return m.group(1)+ds; // user+defaultSuffix

            return u.getId()+ds;
        } else
            return null;
    }

    /**
     * Tries to extract an email address from the user id, or returns null
     */
    private static String extractAddressFromId(String id) {
        Matcher m = EMAIL_ADDRESS_REGEXP.matcher(id);
        if(m.matches())
    		return m.group(1);
    	return null;
    }

    /**
     * Matches strings like "Kohsuke Kawaguchi &lt;kohsuke.kawaguchi@sun.com>"
     * @see #extractAddressFromId(String)
     */
    private static final Pattern EMAIL_ADDRESS_REGEXP = Pattern.compile("^.*<([^>]+)>.*$");

    /**
     * Matches something like "DOMAIN\person"
     */
    private static final Pattern WINDOWS_DOMAIN_REGEXP = Pattern.compile("[^\\\\ ]+\\\\([^\\\\ ]+)");

    /**
     * All registered {@link MailAddressResolver} implementations.
     *
     * @deprecated as of 1.286
     *      Use {@link #all()} for read access and {@link Extension} for registration.
     */
    public static final List<MailAddressResolver> LIST = ExtensionListView.createList(MailAddressResolver.class);

    /**
     * Returns all the registered {@link MailAddressResolver} descriptors.
     */
    public static ExtensionList<MailAddressResolver> all() {
        return Hudson.getInstance().getExtensionList(MailAddressResolver.class);
    }

    private static final Logger LOGGER = Logger.getLogger(MailAddressResolver.class.getName());
}
