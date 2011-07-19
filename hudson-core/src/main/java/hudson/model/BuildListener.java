/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
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

package hudson.model;

import java.util.List;

/**
 * Receives events that happen during a build.
 *
 * @author Kohsuke Kawaguchi
 */
public interface BuildListener extends TaskListener {

    /**
     * Called when a build is started.
     *
     * @param causes
     *      Causes that started a build. See {@link Run#getCauses()}.
     */
    void started(List<Cause> causes);

    /**
     * Called when a build is finished.
     */
    void finished(Result result);
}
