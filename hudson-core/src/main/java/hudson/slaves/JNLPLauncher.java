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
*    Kohsuke Kawaguchi, Stephen Connolly
 *     
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.Util;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * {@link ComputerLauncher} via JNLP.
 *
 * @author Stephen Connolly
 * @author Kohsuke Kawaguchi
*/
public class JNLPLauncher extends ComputerLauncher {
    /**
     * If the slave needs to tunnel the connection to the master,
     * specify the "host:port" here. This can include the special
     * syntax "host:" and ":port" to indicate the default host/port
     * shall be used.
     *
     * <p>
     * Null if no tunneling is necessary.
     *
     * @since 1.250
     */
    public final String tunnel;

    /**
     * Additional JVM arguments. Can be null.
     * @since 1.297
     */
    public final String vmargs;

    @DataBoundConstructor
    public JNLPLauncher(String tunnel, String vmargs) {
        this.tunnel = Util.fixEmptyAndTrim(tunnel);
        this.vmargs = Util.fixEmptyAndTrim(vmargs);
    }

    public JNLPLauncher() {
        this(null,null);
    }

    @Override
    public boolean isLaunchSupported() {
        return false;
    }

    @Override
    public void launch(SlaveComputer computer, TaskListener listener) {
        // do nothing as we cannot self start
    }

    @Extension
    public static final Descriptor<ComputerLauncher> DESCRIPTOR = new Descriptor<ComputerLauncher>() {
        public String getDisplayName() {
            return Messages.JNLPLauncher_displayName();
        }
    };

}
