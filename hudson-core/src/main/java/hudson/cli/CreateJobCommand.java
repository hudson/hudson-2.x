/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

import hudson.model.Hudson;
import hudson.Extension;
import hudson.model.Item;
import org.kohsuke.args4j.Argument;

/**
 * Creates a new job by reading stdin as a configuration XML file.
 * 
 * @author Kohsuke Kawaguchi
 */
@Extension
public class CreateJobCommand extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Creates a new job by reading stdin as a configuration XML file";
    }

    @Argument(metaVar="NAME",usage="Name of the job to create")
    public String name;

    protected int run() throws Exception {
        Hudson h = Hudson.getInstance();
        h.checkPermission(Item.CREATE);

        if (h.getItem(name)!=null) {
            stderr.println("Job '"+name+"' already exists");
            return -1;
        }

        h.createProjectFromXML(name,stdin);
        return 0;
    }
}


