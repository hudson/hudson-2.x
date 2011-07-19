/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
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

package hudson.tools;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import java.util.List;
import java.util.ArrayList;

/**
 * Descriptor for a {@link ToolInstaller}.
 * @since 1.305
 */
public abstract class ToolInstallerDescriptor<T extends ToolInstaller> extends Descriptor<ToolInstaller> {

    /**
     * Controls what kind of {@link ToolInstallation} this installer can be applied to.
     *
     * <p>
     * By default, this method just returns true to everything, claiming it's applicable to any tool installations.
     */
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return true;
    }

    public static DescriptorExtensionList<ToolInstaller,ToolInstallerDescriptor<?>> all() {
        return Hudson.getInstance().<ToolInstaller,ToolInstallerDescriptor<?>>getDescriptorList(ToolInstaller.class);
    }

    /**
     * Filters {@link #all()} by eliminating things that are not applicable to the given type.
     */
    public static List<ToolInstallerDescriptor<?>> for_(Class<? extends ToolInstallation> type) {
        List<ToolInstallerDescriptor<?>> r = new ArrayList<ToolInstallerDescriptor<?>>();
        for (ToolInstallerDescriptor<?> d : all())
            if(d.isApplicable(type))
                r.add(d);
        return r;
    }

}
