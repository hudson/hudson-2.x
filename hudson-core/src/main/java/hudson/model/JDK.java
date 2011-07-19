/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
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

import hudson.util.StreamTaskListener;
import hudson.util.NullStream;
import hudson.util.FormValidation;
import hudson.Launcher;
import hudson.Extension;
import hudson.EnvVars;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.tools.JDKInstaller;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Information about JDK installation.
 *
 * @author Kohsuke Kawaguchi
 */
public final class JDK extends ToolInstallation implements NodeSpecific<JDK>, EnvironmentSpecific<JDK> {
    /**
     * @deprecated since 2009-02-25
     */
    @Deprecated // kept for backward compatibility - use getHome() instead
    private transient String javaHome;

    public JDK(String name, String javaHome) {
        super(name, javaHome, Collections.<ToolProperty<?>>emptyList());
    }

    @DataBoundConstructor
    public JDK(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /**
     * install directory.
     *
     * @deprecated as of 1.304
     *      Use {@link #getHome()}
     */
    public String getJavaHome() {
        return getHome();
    }

    /**
     * Gets the path to the bin directory.
     */
    public File getBinDir() {
        return new File(getHome(),"bin");
    }
    /**
     * Gets the path to 'java'.
     */
    public File getExecutable() {
        String execName = (File.separatorChar == '\\') ? "java.exe" : "java";
        return new File(getHome(),"bin/"+execName);
    }

    /**
     * Returns true if the executable exists.
     */
    public boolean getExists() {
        return getExecutable().exists();
    }

    /**
     * Sets PATH and JAVA_HOME from this JDK.
     */
    public void buildEnvVars(Map<String,String> env) {
        // see EnvVars javadoc for why this adss PATH.
        env.put("PATH+JDK",getHome()+"/bin");
        env.put("JAVA_HOME",getHome());
    }

    public JDK forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new JDK(getName(), translateFor(node, log));
    }

    public JDK forEnvironment(EnvVars environment) {
        return new JDK(getName(), environment.expand(getHome()));
    }

    /**
     * Checks if "java" is in PATH on the given node.
     *
     * <p>
     * If it's not, then the user must specify a configured JDK,
     * so this is often useful for form field validation.
     */
    public static boolean isDefaultJDKValid(Node n) {
        try {
            TaskListener listener = new StreamTaskListener(new NullStream());
            Launcher launcher = n.createLauncher(listener);
            return launcher.launch().cmds("java","-fullversion").stdout(listener).join()==0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<JDK> {

        public String getDisplayName() {
            return "JDK"; // XXX I18N
        }

        public @Override JDK[] getInstallations() {
            return Hudson.getInstance().getJDKs().toArray(new JDK[0]);
        }

        // this isn't really synchronized well since the list is Hudson.jdks :(
        public @Override synchronized void setInstallations(JDK... jdks) {
            List<JDK> list = Hudson.getInstance().getJDKs();
            list.clear();
            list.addAll(Arrays.asList(jdks));
        }

        @Override
        public List<JDKInstaller> getDefaultInstallers() {
            return Collections.singletonList(new JDKInstaller(null,false));
        }

        /**
         * Checks if the JAVA_HOME is a valid JAVA_HOME path.
         */
        public FormValidation doCheckHome(@QueryParameter File value) {
            // this can be used to check the existence of a file on the server, so needs to be protected
            Hudson.getInstance().checkPermission(Hudson.ADMINISTER);

            if(value.getPath().equals(""))
                return FormValidation.ok();

            if(!value.isDirectory())
                return FormValidation.error(Messages.Hudson_NotADirectory(value));

            File toolsJar = new File(value,"lib/tools.jar");
            File mac = new File(value,"lib/dt.jar");
            if(!toolsJar.exists() && !mac.exists())
                return FormValidation.error(Messages.Hudson_NotJDKDir(value));

            return FormValidation.ok();
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }
    }

    public static class ConverterImpl extends ToolConverter {
        public ConverterImpl(XStream2 xstream) { super(xstream); }
        @Override protected String oldHomeField(ToolInstallation obj) {
            return ((JDK)obj).javaHome;
        }
    }
}
