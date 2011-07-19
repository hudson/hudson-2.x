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
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.util.IOUtils;
import java.io.File;
import java.io.IOException;
import org.kohsuke.args4j.Argument;

/**
 * Updates or creates a job by reading stdin as a configuration XML file.
 * 
 * @author Henrik Lynggaard Hansen
 */
@Extension
public class UpdateJobCommand extends CLICommand {

    @Override
    public String getShortDescription() {
        return "Updates and potentionally creates a job by reading stdin as a configuration XML file.";
    }
    
    @Argument(metaVar = "NAME", usage = "Name of the job to update", required = true)
    public String name;
    @Argument(metaVar = "CREATE", usage = "Create the job if needed", index = 1, required = true)
    public Boolean create;

    protected int run() throws Exception {
        Hudson h = Hudson.getInstance();

        TopLevelItem item = h.getItem(name);

        if (item == null && !create) {
            stderr.println("Job '" + name + "' does not exist and create is set to false");
            return -1;
        }

        if (item == null) {
            h.checkPermission(Item.CREATE);
            h.createProjectFromXML(name, stdin);
        } else {
            try {               
                h.checkPermission(Job.CONFIGURE);
                               
                File rootDirOfJob = new File(new File(h.getRootDir(), "jobs"), name);
                // place it as config.xml
                File configXml = Items.getConfigFile(rootDirOfJob).getFile();
                IOUtils.copy(stdin, configXml);
                
                item = h.reloadProjectFromDisk(configXml.getParentFile());                               
            } catch (IOException e) {
                throw e;
            }
        }
        return 0;
    }
}
