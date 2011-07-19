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

import static hudson.init.InitMilestone.JOB_LOADED;
import hudson.init.Initializer;
import hudson.model.Hudson;
import hudson.model.Messages;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Prepares userContent folder and put a readme if it doesn't exist.
 * @author Kohsuke Kawaguchi
 */
public class InitialUserContent {
    @Initializer(after=JOB_LOADED)
    public static void init(Hudson h) throws IOException {
        File userContentDir = new File(h.getRootDir(), "userContent");
        if(!userContentDir.exists()) {
            userContentDir.mkdirs();
            FileUtils.writeStringToFile(new File(userContentDir,"readme.txt"), Messages.Hudson_USER_CONTENT_README());
        }
    }
}
