/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import org.kohsuke.args4j.Argument;

/**
 * Sets the result of the current build. Works only if invoked from within a build.
 * 
 * @author Kohsuke Kawaguchi
 */
@Extension
public class SetBuildResultCommand extends CommandDuringBuild {
    @Argument(metaVar="RESULT",required=true)
    public Result result;

    @Override
    public String getShortDescription() {
        return "Sets the result of the current build. Works only if invoked from within a build.";
    }

    @Override
    protected int run() throws Exception {
        Run r = getCurrentlyBuilding();
        r.getParent().checkPermission(Item.BUILD);
        r.setResult(result);
        return 0;
    }
}
