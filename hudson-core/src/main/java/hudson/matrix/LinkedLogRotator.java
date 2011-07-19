/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.matrix;

import hudson.model.Job;
import hudson.tasks.LogRotator;

import java.io.IOException;

/**
 * {@link LogRotator} for {@link MatrixConfiguration},
 * which discards the builds if and only if it's discarded
 * in the parent.
 *
 * <p>
 * Because of the serialization compatibility, we can't easily
 * refactor {@link LogRotator} into a contract and an implementation. 
 *
 * @author Kohsuke Kawaguchi
 */
final class LinkedLogRotator extends LogRotator {
    LinkedLogRotator(int artifactDaysToKeep, int artifactNumToKeep) {
        super(-1, -1, artifactDaysToKeep, artifactNumToKeep);
    }

    /**
     * @deprecated since 1.369
     *     Use {@link #LinkedLogRotator(int, int)}
     */
    LinkedLogRotator() {
        super(-1, -1, -1, -1);
    }

    @Override
    public void perform(Job _job) throws IOException, InterruptedException {
        // Let superclass handle clearing artifacts, if configured:
        super.perform(_job);
        MatrixConfiguration job = (MatrixConfiguration) _job;

        // copy it to the array because we'll be deleting builds as we go.
        for( MatrixRun r : job.getBuilds().toArray(new MatrixRun[0]) ) {
            if(job.getParent().getBuildByNumber(r.getNumber())==null)
                r.delete();
        }

        if(!job.isActiveConfiguration() && job.getLastBuild()==null)
            job.delete();
    }
}
