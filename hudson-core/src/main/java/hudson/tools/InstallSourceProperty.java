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

import hudson.Extension;
import hudson.util.DescribableList;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.io.IOException;

/**
 * {@link ToolProperty} that shows auto installation options.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.305
 */
public class InstallSourceProperty extends ToolProperty<ToolInstallation> {
    // TODO: get the proper Saveable
    //TODO: review and check whether we can do it private
    public final DescribableList<ToolInstaller, Descriptor<ToolInstaller>> installers =
            new DescribableList<ToolInstaller, Descriptor<ToolInstaller>>(Saveable.NOOP);

    @DataBoundConstructor
    public InstallSourceProperty(List<? extends ToolInstaller> installers) throws IOException {
        if (installers != null) {
            this.installers.replaceBy(installers);
        }
    }

    public DescribableList<ToolInstaller, Descriptor<ToolInstaller>> getInstallers() {
        return installers;
    }

    @Override
    public void setTool(ToolInstallation t) {
        super.setTool(t);
        for (ToolInstaller installer : installers)
            installer.setTool(t);
    }

    public Class<ToolInstallation> type() {
        return ToolInstallation.class;
    }

    @Extension
    public static class DescriptorImpl extends ToolPropertyDescriptor {
        public String getDisplayName() {
            return Messages.InstallSourceProperty_DescriptorImpl_displayName();
        }
    }
}
