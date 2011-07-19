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
*    Kohsuke Kawaguchi, Yahoo! Inc., Seiji Sogabe, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.security;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.util.RobustReflectionConverter;
import hudson.Extension;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.core.JVM;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link GlobalMatrixAuthorizationStrategy} plus per-project ACL.
 *
 * <p>
 * Per-project ACL is stored in {@link AuthorizationMatrixProperty}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ProjectMatrixAuthorizationStrategy extends GlobalMatrixAuthorizationStrategy {
    @Override
    public ACL getACL(Job<?,?> project) {
        AuthorizationMatrixProperty amp = project.getProperty(AuthorizationMatrixProperty.class);
        if (amp != null) {
            return amp.getACL().newInheritingACL(getRootACL());
        } else {
            return getRootACL();
        }
    }

    @Override
    public Set<String> getGroups() {
        Set<String> r = new HashSet<String>();
        r.addAll(super.getGroups());
        for (Job<?,?> j : Hudson.getInstance().getItems(Job.class)) {
            AuthorizationMatrixProperty amp = j.getProperty(AuthorizationMatrixProperty.class);
            if (amp != null)
                r.addAll(amp.getGroups());
        }
        return r;
    }

    @Extension
    public static final Descriptor<AuthorizationStrategy> DESCRIPTOR = new DescriptorImpl() {
        @Override
        protected GlobalMatrixAuthorizationStrategy create() {
            return new ProjectMatrixAuthorizationStrategy();
        }

        @Override
        public String getDisplayName() {
            return Messages.ProjectMatrixAuthorizationStrategy_DisplayName();
        }
    };

    public static class ConverterImpl extends GlobalMatrixAuthorizationStrategy.ConverterImpl {
        private RobustReflectionConverter ref;

        public ConverterImpl(Mapper m) {
            ref = new RobustReflectionConverter(m,new JVM().bestReflectionProvider());
        }

        @Override
        protected GlobalMatrixAuthorizationStrategy create() {
            return new ProjectMatrixAuthorizationStrategy();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String name = reader.peekNextChild();
            if(name!=null && (name.equals("permission") || name.equals("useProjectSecurity")))
                // the proper serialization form
                return super.unmarshal(reader, context);
            else
                // remain compatible with earlier problem where we used reflection converter
                return ref.unmarshal(reader,context);
        }

        @Override
        public boolean canConvert(Class type) {
            return type==ProjectMatrixAuthorizationStrategy.class;
        }
    }
}

