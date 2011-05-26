/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.eventspy_30.handler;


import org.apache.maven.cli.BatchModeMavenTransferListener;
import org.apache.maven.cli.QuietMavenTransferListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.hudsonci.maven.eventspy.common.DocumentReference;
import org.hudsonci.maven.eventspy_30.EventSpyHandler;
import org.sonatype.aether.transfer.TransferListener;

import javax.inject.Named;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.apache.maven.cli.MavenCli.DEFAULT_USER_TOOLCHAINS_FILE;

/**
 * Handles {@link MavenExecutionRequest} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
public class MavenExecutionRequestHandler
    extends EventSpyHandler<MavenExecutionRequest>
{
    public void handle(final MavenExecutionRequest event) throws Exception {
        log.debug("Execution request: {}", event);
        
        // Configure a batch listener unless a quiet listener is already added
        TransferListener listener = event.getTransferListener();
        if (!(listener instanceof QuietMavenTransferListener)) {
            event.setTransferListener(new BatchModeMavenTransferListener(System.out));
            log.debug("Configured batch mode transfer listener");
        }

        ProfileLogger.logRequestProfiles( event ); // TODO: is this needed anymore?

        configureToolChains(event);

        // TODO: See if we need to actually handle TransferEvent's via handlers too, or if the other aether events cover our needs.
    }

    private void configureToolChains(final MavenExecutionRequest event) throws IOException {
        // If there is a toolchains document, then write its content to file and configure the request to use it
        DocumentReference document = getCallback().getToolChainsDocument();
        if (document == null) return;

        if (event.getUserToolchainsFile() != DEFAULT_USER_TOOLCHAINS_FILE) {
            log.warn("Custom tool-chains file configured via command-line as well as via document; document taking precedence");
        }

        log.info("Using tool-chains document ID: {}", document.getId());
        log.trace("Content:\n{}", document.getContent()); // FIXME: May contain sensitive data?

        File file = new File(getCallback().getMavenContextDirectory(), "toolchains.xml");
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.warn("Failed to create directory structure for: {}", file);
            }
        }

        // Document should not really contain sensitive details, so just leave it around
        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.write(document.getContent());
        }
        finally {
            writer.close();
        }

        log.debug("Wrote toolchains.xml: {}", file);
        event.setUserToolchainsFile(file);
    }
}
