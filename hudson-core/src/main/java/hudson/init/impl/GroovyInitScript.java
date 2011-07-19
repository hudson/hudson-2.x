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

package hudson.init.impl;

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import hudson.FilePath;
import hudson.model.Hudson;
import static hudson.init.InitMilestone.JOB_LOADED;
import hudson.init.Initializer;
import org.apache.commons.io.FileUtils;

/**
 * Run the initialization script, if it exists.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GroovyInitScript {
    @Initializer(after=JOB_LOADED)
    public static void init(Hudson h) throws IOException {
        URL bundledInitScript = h.servletContext.getResource("/WEB-INF/init.groovy");
        if (bundledInitScript!=null) {
            LOGGER.info("Executing bundled init script: "+bundledInitScript);
            execute(new GroovyCodeSource(bundledInitScript));
        }

        File initScript = new File(h.getRootDir(),"init.groovy");
        if(initScript.exists()) {
            LOGGER.info("Executing "+initScript);
            execute(new GroovyCodeSource(initScript));
        }
    }

    private static void execute(GroovyCodeSource initScript) throws IOException {
        GroovyShell shell = new GroovyShell(Hudson.getInstance().getPluginManager().uberClassLoader);
        shell.evaluate(initScript);
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyInitScript.class.getName());
}
