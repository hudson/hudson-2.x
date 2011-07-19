/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.cli;

import groovy.lang.GroovyShell;
import groovy.lang.Binding;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Run;
import hudson.remoting.Callable;
import hudson.AbortException;
import hudson.Extension;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Executes the specified groovy script.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class GroovyCommand extends CLICommand implements Serializable {
    @Override
    public String getShortDescription() {
        return "Executes the specified Groovy script";
    }

    @Argument(metaVar="SCRIPT",usage="Script to be executed. File, URL or '=' to represent stdin.")
    public String script;

    /**
     * Remaining arguments.
     */
    @Argument(index=1)
    public List<String> remaining = new ArrayList<String>();

    protected int run() throws Exception {
        // this allows the caller to manipulate the JVM state, so require the admin privilege.
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);

        Binding binding = new Binding();
        binding.setProperty("out",new PrintWriter(stdout,true));
        String j = getClientEnvironmentVariable("JOB_NAME");
        if (j!=null) {
            Item job = Hudson.getInstance().getItemByFullName(j);
            binding.setProperty("currentJob", job);
            String b = getClientEnvironmentVariable("BUILD_NUMBER");
            if (b!=null && job instanceof AbstractProject) {
                Run r = ((AbstractProject) job).getBuildByNumber(Integer.parseInt(b));
                binding.setProperty("currentBuild", r);
            }
        }

        GroovyShell groovy = new GroovyShell(binding);
        groovy.run(loadScript(),"RemoteClass",remaining.toArray(new String[remaining.size()]));
        return 0;
    }

    /**
     * Loads the script from the argument.
     */
    private String loadScript() throws CmdLineException, IOException, InterruptedException {
        if(script==null)
            throw new CmdLineException(null, "No script is specified");
        return channel.call(new Callable<String,IOException>() {
            public String call() throws IOException {
                if(script.equals("="))
                    return IOUtils.toString(System.in);

                File f = new File(script);
                if(f.exists())
                    return FileUtils.readFileToString(f);

                URL url;
                try {
                    url = new URL(script);
                } catch (MalformedURLException e) {
                    throw new AbortException("Unable to find a script "+script);
                }
                InputStream s = url.openStream();
                try {
                    return IOUtils.toString(s);
                } finally {
                    s.close();
                }
            }
        });
    }
}

