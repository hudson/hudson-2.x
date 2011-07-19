/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       
 *
 *******************************************************************************/ 

package hudson.matrix;

import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.model.Hudson;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

/**
 * {@link Descriptor} for {@link Axis}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AxisDescriptor extends Descriptor<Axis> {
    protected AxisDescriptor(Class<? extends Axis> clazz) {
        super(clazz);
    }

    protected AxisDescriptor() {
    }

    /**
     * Return false if the user shouldn't be able to create thie axis from the UI.
     */
    public boolean isInstantiable() {
        return true;
    }

    /**
     * Makes sure that the given name is good as a axis name.
     */
    public FormValidation doCheckName(@QueryParameter String value) {
        if(Util.fixEmpty(value)==null)
            return FormValidation.ok();

        try {
            Hudson.checkGoodName(value);
            return FormValidation.ok();
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }
    }
}
