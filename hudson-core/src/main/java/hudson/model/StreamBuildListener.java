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

import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * {@link BuildListener} that writes to an {@link OutputStream}.
 *
 * This class is remotable.
 * 
 * @author Kohsuke Kawaguchi
 */
public class StreamBuildListener extends StreamTaskListener implements BuildListener {
    public StreamBuildListener(OutputStream out, Charset charset) {
        super(out, charset);
    }

    public StreamBuildListener(File out, Charset charset) throws IOException {
        super(out, charset);
    }

    public StreamBuildListener(OutputStream w) {
        super(w);
    }

    /**
     * @deprecated as of 1.349
     *      The caller should use {@link #StreamBuildListener(OutputStream, Charset)} to pass in
     *      the charset and output stream separately, so that this class can handle encoding correctly.
     */
    public StreamBuildListener(PrintStream w) {
        super(w);
    }

    public StreamBuildListener(PrintStream w, Charset charset) {
        super(w,charset);
    }

    public void started(List<Cause> causes) {
        PrintStream l = getLogger();
        if (causes==null || causes.isEmpty())
            l.println("Started");
        else for (Cause cause : causes) {
            cause.print(this);
        }
    }

    public void finished(Result result) {
        getLogger().println("Finished: "+result);
    }

    private static final long serialVersionUID = 1L;
}
