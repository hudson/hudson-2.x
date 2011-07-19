/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.maven.eventspy_30.handler;


import org.apache.maven.cli.BatchModeMavenTransferListener;
import org.apache.maven.cli.QuietMavenTransferListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.eclipse.hudson.maven.eventspy_30.EventSpyHandler;
import org.model.hudson.maven.eventspy.common.DocumentReference;
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
