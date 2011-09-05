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
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.ComputerSet;
import hudson.model.Environment;
import hudson.model.Node;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * {@link NodeProperty} that sets additional environment variables.
 *
 * @since 1.286
 */
public class EnvironmentVariablesNodeProperty extends NodeProperty<Node> {

    /**
     * Slave-specific environment variables
     */
    private final EnvVars envVars;
    
    @DataBoundConstructor
    public EnvironmentVariablesNodeProperty(List<Entry> env) {
        this.envVars = toMap(env);
    }

    public EnvironmentVariablesNodeProperty(Entry... env) {
        this(Arrays.asList(env));
    }
	
    public EnvVars getEnvVars() {
    	return envVars;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
    	return Environment.create(envVars);
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {

        @Override
		public String getDisplayName() {
			return Messages.EnvironmentVariablesNodeProperty_displayName();
		}

        public String getHelpPage() {
            // yes, I know this is a work around.
            ComputerSet object = Stapler.getCurrentRequest().findAncestorObject(ComputerSet.class);
            if (object != null) {
                // we're on a node configuration page, show show that help page
                return "/help/system-config/nodeEnvironmentVariables.html";
            } else {
                // show the help for the global config page
                return "/help/system-config/globalEnvironmentVariables.html";
            }
        }
    }
	
	public static class Entry {
        //TODO: review and check whether we can do it private
		public String key, value;

		@DataBoundConstructor
		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
	
	private static EnvVars toMap(List<Entry> entries) {
		EnvVars map = new EnvVars();
        if (entries!=null)
            for (Entry entry: entries)
                map.put(entry.key,entry.value);
		return map;
	}

}
