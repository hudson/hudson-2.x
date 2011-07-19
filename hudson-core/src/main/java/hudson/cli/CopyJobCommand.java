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
import hudson.model.TopLevelItem;
import hudson.Extension;
import hudson.model.Item;
import org.kohsuke.args4j.Argument;


/**
 * Copies a job from CLI.
 * 
 * @author Kohsuke Kawaguchi
 */
@Extension
public class CopyJobCommand extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Copies a job";
    }

    @Argument(metaVar="SRC",usage="Name of the job to copy",required=true)
    public TopLevelItem src;

    @Argument(metaVar="DST",usage="Name of the new job to be created.",index=1,required=true)
    public String dst;

    protected int run() throws Exception {
        Hudson h = Hudson.getInstance();
        h.checkPermission(Item.CREATE);

        if (h.getItem(dst)!=null) {
            stderr.println("Job '"+dst+"' already exists");
            return -1;
        }
        
        h.copy(src,dst);
        return 0;
    }
}

