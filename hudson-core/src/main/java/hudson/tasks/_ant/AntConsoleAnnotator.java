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

package hudson.tasks._ant;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Filter {@link OutputStream} that places an annotation that marks Ant target execution.
 * 
 * @author Kohsuke Kawaguchi
 * @sine 1.349
 */
public class AntConsoleAnnotator extends LineTransformationOutputStream {
    private final OutputStream out;
    private final Charset charset;

    private boolean seenEmptyLine;

    public AntConsoleAnnotator(OutputStream out, Charset charset) {
        this.out = out;
        this.charset = charset;
    }

    @Override
    protected void eol(byte[] b, int len) throws IOException {
        String line = charset.decode(ByteBuffer.wrap(b, 0, len)).toString();

        // trim off CR/LF from the end
        line = trimEOL(line);

        if (seenEmptyLine && endsWith(line,':') && line.indexOf(' ')<0)
            // put the annotation
            new AntTargetNote().encodeTo(out);

        if (line.equals("BUILD SUCCESSFUL") || line.equals("BUILD FAILED"))
            new AntOutcomeNote().encodeTo(out);

        seenEmptyLine = line.length()==0;
        out.write(b,0,len);
    }

    private boolean endsWith(String line, char c) {
        int len = line.length();
        return len>0 && line.charAt(len-1)==c;
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }

}
