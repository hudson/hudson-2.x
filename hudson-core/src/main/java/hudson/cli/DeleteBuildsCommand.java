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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Deletes builds records in a bulk.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DeleteBuildsCommand extends AbstractBuildRangeCommand {
    @Override
    public String getShortDescription() {
        return "Deletes build record(s)";
    }

    @Override
    protected void printUsageSummary(PrintStream stderr) {
        stderr.println(
            "Delete build records of a specified job, possibly in a bulk. "
        );
    }

    @Override
    protected int act(List<AbstractBuild<?, ?>> builds) throws IOException {
        job.checkPermission(Run.DELETE);

        for (AbstractBuild build : builds)
            build.delete();

        stdout.println("Deleted "+builds.size()+" builds");

        return 0;
    }

}
