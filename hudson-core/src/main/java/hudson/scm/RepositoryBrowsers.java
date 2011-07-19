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
*    Kohsuke Kawaguchi, Daniel Dyer, Stephen Connolly
 *     
 *
 *******************************************************************************/ 

package hudson.scm;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Descriptor.FormException;
import hudson.scm.browsers.*;
import hudson.util.DescriptorList;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * List of all installed {@link RepositoryBrowsers}.
 *
 * @author Kohsuke Kawaguchi
 */
public class RepositoryBrowsers {
    /**
     * List of all installed {@link RepositoryBrowsers}.
     *
     * @deprecated as of 1.286.
     *      Use {@link RepositoryBrowser#all()} for read access and {@link Extension} for registration.
     */
    public static final List<Descriptor<RepositoryBrowser<?>>> LIST = new DescriptorList<RepositoryBrowser<?>>((Class)RepositoryBrowser.class);

    /**
     * Only returns those {@link RepositoryBrowser} descriptors that extend from the given type.
     */
    public static List<Descriptor<RepositoryBrowser<?>>> filter(Class<? extends RepositoryBrowser> t) {
        List<Descriptor<RepositoryBrowser<?>>> r = new ArrayList<Descriptor<RepositoryBrowser<?>>>();
        for (Descriptor<RepositoryBrowser<?>> d : RepositoryBrowser.all())
            if(d.isSubTypeOf(t))
                r.add(d);
        return r;
    }

    /**
     * Creates an instance of {@link RepositoryBrowser} from a form submission.
     *
     * @deprecated since 2008-06-19.
     *      Use {@link #createInstance(Class, StaplerRequest, JSONObject, String)}.
     */
    public static <T extends RepositoryBrowser>
    T createInstance(Class<T> type, StaplerRequest req, String fieldName) throws FormException {
        List<Descriptor<RepositoryBrowser<?>>> list = filter(type);
        String value = req.getParameter(fieldName);
        if(value==null || value.equals("auto"))
            return null;

        return type.cast(list.get(Integer.parseInt(value)).newInstance(req,null/*TODO*/));
    }

    /**
     * Creates an instance of {@link RepositoryBrowser} from a form submission.
     *
     * @since 1.227
     */
    public static <T extends RepositoryBrowser>
    T createInstance(Class<T> type, StaplerRequest req, JSONObject parent, String fieldName) throws FormException {
        JSONObject o = (JSONObject)parent.get(fieldName);
        if(o==null) return null;

        return req.bindJSON(type,o);
    }
}
