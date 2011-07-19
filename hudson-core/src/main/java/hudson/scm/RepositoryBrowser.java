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

package hudson.scm;

import hudson.ExtensionPoint;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.MalformedURLException;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Connects Hudson to repository browsers like ViewCVS or FishEye,
 * so that Hudson can generate links to them. 
 *
 * <p>
 * {@link RepositoryBrowser} instance is normally created as
 * a result of job configuration, and  stores immutable
 * configuration information (such as the URL of the FishEye site).
 *
 * <p>
 * {@link RepositoryBrowser} is persisted with {@link SCM}.
 *
 * <p>
 * To have Hudson recognize {@link RepositoryBrowser}, put {@link Extension} on your {@link Descriptor}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.89
 * @see RepositoryBrowsers
 */
@ExportedBean
public abstract class RepositoryBrowser<E extends ChangeLogSet.Entry> extends AbstractDescribableImpl<RepositoryBrowser<?>> implements ExtensionPoint, Serializable {
    /**
     * Determines the link to the given change set.
     *
     * @return
     *      null if this repository browser doesn't have any meaningful
     *      URL for a change set (for example, ViewCVS doesn't have
     *      any page for a change set, whereas FishEye does.)
     */
    public abstract URL getChangeSetLink(E changeSet) throws IOException;

    /**
     * If the given string starts with '/', return a string that removes it.
     */
    protected static String trimHeadSlash(String s) {
        if(s.startsWith("/"))   return s.substring(1);
        return s;
    }

    /**
     * Normalize the URL so that it ends with '/'.
     * <p>
     * An attention is paid to preserve the query parameters in URL if any. 
     */
    protected static URL normalizeToEndWithSlash(URL url) {
        if(url.getPath().endsWith("/"))
            return url;

        // normalize
        String q = url.getQuery();
        q = q!=null?('?'+q):"";
        try {
            return new URL(url,url.getPath()+'/'+q);
        } catch (MalformedURLException e) {
            // impossible
            throw new Error(e);
        }
    }

    /**
     * Returns all the registered {@link RepositoryBrowser} descriptors.
     */
    public static DescriptorExtensionList<RepositoryBrowser<?>,Descriptor<RepositoryBrowser<?>>> all() {
        return (DescriptorExtensionList)Hudson.getInstance().getDescriptorList(RepositoryBrowser.class);
    }

    private static final long serialVersionUID = 1L;
}
