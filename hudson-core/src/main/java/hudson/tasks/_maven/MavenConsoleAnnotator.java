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

package hudson.tasks._maven;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

/**
 * Filter {@link OutputStream} that places annotations that marks various Maven outputs.
 *
 * @author Kohsuke Kawaguchi
 */
public class MavenConsoleAnnotator extends LineTransformationOutputStream {
    private final OutputStream out;
    private final Charset charset;

    public MavenConsoleAnnotator(OutputStream out, Charset charset) {
        this.out = out;
        this.charset = charset;
    }

    @Override
    protected void eol(byte[] b, int len) throws IOException {
        String line = charset.decode(ByteBuffer.wrap(b, 0, len)).toString();

        // trim off CR/LF from the end
        line = trimEOL(line);

        // TODO:
        // we need more support for conveniently putting annotations in the middle of the line, not just at the beginning
        // we also need the ability for an extension point to have notes hook into the processing

        Matcher m = MavenMojoNote.PATTERN.matcher(line);
        if (m.matches())
            new MavenMojoNote().encodeTo(out);

        m = MavenWarningNote.PATTERN.matcher(line);
        if (m.find())
            new MavenWarningNote().encodeTo(out);

        m = MavenErrorNote.PATTERN.matcher(line);
        if (m.find())
            new MavenErrorNote().encodeTo(out);

        out.write(b,0,len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }
}
