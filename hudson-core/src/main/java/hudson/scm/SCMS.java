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

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.util.DescriptorList;
import hudson.DescriptorExtensionList;
import hudson.Extension;

import java.util.List;

import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;

/**
 * List of all installed SCMs.
 * 
 * @author Kohsuke Kawaguchi
 */
public class SCMS {
    /**
     * List of all installed SCMs.
     * @deprecated as of 1.286
     *      Use {@link SCM#all()} for read access and {@link Extension} for registration.
     */
    public static final List<SCMDescriptor<?>> SCMS = (List)new DescriptorList<SCM>(SCM.class);

    /**
     * Parses {@link SCM} configuration from the submitted form.
     *
     * @param target
     *      The project for which this SCM is configured to.
     */
    public static SCM parseSCM(StaplerRequest req, AbstractProject target) throws FormException, ServletException {
        String scm = req.getParameter("scm");
        if(scm==null)   return new NullSCM();

        int scmidx = Integer.parseInt(scm);
        SCMDescriptor<?> d = SCM._for(target).get(scmidx);
        d.generation++;
        return d.newInstance(req, req.getSubmittedForm().getJSONObject("scm"));
    }

    /**
     * @deprecated as of 1.294
     *      Use {@link #parseSCM(StaplerRequest, AbstractProject)} and pass in the caller's project type.
     */
    public static SCM parseSCM(StaplerRequest req) throws FormException, ServletException {
        return parseSCM(req,null);
    }

}
