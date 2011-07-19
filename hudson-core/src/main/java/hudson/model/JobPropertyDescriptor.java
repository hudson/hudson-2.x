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

package hudson.model;

import org.kohsuke.stapler.StaplerRequest;
import org.jvnet.tiger_types.Types;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import net.sf.json.JSONObject;

/**
 * {@link Descriptor} for {@link JobProperty}.
 * 
 * @author Kohsuke Kawaguchi
 * @since 1.72
 */
public abstract class JobPropertyDescriptor extends Descriptor<JobProperty<?>> {
    protected JobPropertyDescriptor(Class<? extends JobProperty<?>> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link JobProperty} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected JobPropertyDescriptor() {
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *      null to avoid setting an instance of {@link JobProperty} to the target project.
     */
    @Override
    public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        // JobPropertyDescriptors are bit different in that we allow them even without any user-visible configuration parameter,
        // so replace the lack of form data by an empty one. 
        if(formData.isNullObject()) formData=new JSONObject();

        return super.newInstance(req, formData);
    }

    /**
     * Returns true if this {@link JobProperty} type is applicable to the
     * given job type.
     * 
     * <p>
     * The default implementation of this method checks if the given job type is assignable to 'J' of
     * {@link JobProperty}<tt>&lt;J></tt>, but subtypes can extend this to change this behavior.
     *
     * @return
     *      true to indicate applicable, in which case the property will be
     *      displayed in the configuration screen of this job.
     */
    public boolean isApplicable(Class<? extends Job> jobType) {
        Type parameterization = Types.getBaseClass(clazz, JobProperty.class);
        if (parameterization instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) parameterization;
            Class applicable = Types.erasure(Types.getTypeArgument(pt, 0));
            return applicable.isAssignableFrom(jobType);
        } else {
            throw new AssertionError(clazz+" doesn't properly parameterize JobProperty. The isApplicable() method must be overriden.");
        }
    }

    /**
     * Gets the {@link JobPropertyDescriptor}s applicable for a given job type.
     */
    public static List<JobPropertyDescriptor> getPropertyDescriptors(Class<? extends Job> clazz) {
        List<JobPropertyDescriptor> r = new ArrayList<JobPropertyDescriptor>();
        for (JobPropertyDescriptor p : all())
            if(p.isApplicable(clazz))
                r.add(p);
        return r;
    }

    public static Collection<JobPropertyDescriptor> all() {
        return (Collection)Hudson.getInstance().getDescriptorList(JobProperty.class);
    }
}
