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

package hudson.slaves;

import hudson.Extension;
import hudson.model.ComputerSet;
import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.model.Node;
import hudson.model.Hudson;
import hudson.util.DescriptorList;
import hudson.util.FormValidation;
import hudson.DescriptorExtensionList;
import hudson.Util;
import hudson.model.Failure;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;

/**
 * {@link Descriptor} for {@link Slave}.
 *
 * <h2>Views</h2>
 * <p>
 * This object needs to have <tt>newInstanceDetail.jelly</tt> view, which shows up in
 * <tt>http://server/hudson/computers/new</tt> page as an explanation of this job type.
 *
 * <h2>Other Implementation Notes</h2>
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class NodeDescriptor extends Descriptor<Node> {
    protected NodeDescriptor(Class<? extends Node> clazz) {
        super(clazz);
    }

    protected NodeDescriptor() {
    }

    /**
     * Can the administrator create this type of nodes from UI?
     *
     * Return false if it only makes sense for programs to create it, not through the "new node" UI.
     */
    public boolean isInstantiable() {
        return true;
    }

    public final String newInstanceDetailPage() {
        return '/'+clazz.getName().replace('.','/').replace('$','/')+"/newInstanceDetail.jelly";
    }

    /**
     * Handles the form submission from the "/computer/new" page, which is the first form for creating a new node.
     * By default, it shows the configuration page for entering details, but subtypes can override this differently.
     *
     * @param name
     *      Name of the new node.
     */
    public void handleNewNodePage(ComputerSet computerSet, String name, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        computerSet.checkName(name);
        req.setAttribute("descriptor", this);
        req.getView(computerSet,"_new.jelly").forward(req,rsp);
    }

    @Override
    public String getConfigPage() {
        return getViewPage(clazz, "configure-entries.jelly");
    }

    public FormValidation doCheckName(@QueryParameter String value ) {
        String name = Util.fixEmptyAndTrim(value);
        if(name==null)
            return FormValidation.error(Messages.NodeDescripter_CheckName_Mandatory());
        try {
            Hudson.checkGoodName(name);
        } catch (Failure f) {
            return FormValidation.error(f.getMessage());
        }
        return FormValidation.ok();
    }

    /**
     * Returns all the registered {@link NodeDescriptor} descriptors.
     */
    public static DescriptorExtensionList<Node,NodeDescriptor> all() {
        return Hudson.getInstance().<Node,NodeDescriptor>getDescriptorList(Node.class);
    }

    /**
     * All the registered instances.
     * @deprecated as of 1.286
     *      Use {@link #all()} for read access, and {@link Extension} for registration.
     */
    public static final DescriptorList<Node> ALL = new DescriptorList<Node>(Node.class);

    public static List<NodeDescriptor> allInstantiable() {
        List<NodeDescriptor> r = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor d : all())
            if(d.isInstantiable())
                r.add(d);
        return r;
    }
}
