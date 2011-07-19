/*******************************************************************************
 *
 * Copyright (c) 2009, Yahoo!, Inc.
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

package org.jvnet.hudson.test;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

import java.io.IOException;
import java.io.Serializable;

public class TouchBuilder extends Builder implements Serializable {
        @Override
        public boolean perform(AbstractBuild<?, ?> build,
                               Launcher launcher, BuildListener listener)
                throws InterruptedException, IOException {
            for (FilePath f : build.getWorkspace().list()) {
                f.touch(System.currentTimeMillis());
            }
            return true;
        }
    }
