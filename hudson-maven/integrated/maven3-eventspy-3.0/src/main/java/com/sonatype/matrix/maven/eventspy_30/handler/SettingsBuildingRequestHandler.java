/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import com.sonatype.matrix.maven.eventspy.common.DocumentReference;
import com.sonatype.matrix.maven.eventspy_30.EventSpyHandler;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.StringSettingsSource;

import javax.inject.Named;

import static org.apache.maven.cli.MavenCli.DEFAULT_GLOBAL_SETTINGS_FILE;
import static org.apache.maven.cli.MavenCli.DEFAULT_USER_SETTINGS_FILE;

/**
 * Handles {@link SettingsBuildingRequest} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
public class SettingsBuildingRequestHandler
    extends EventSpyHandler<SettingsBuildingRequest>
{
    public void handle(final SettingsBuildingRequest event) throws Exception {
        log.debug("Settings request: {}", event);

        DocumentReference document;

        // TODO: Support debug option to write document to disk

        document = getCallback().getSettingsDocument();
        log.debug("Settings document: {}", document);

        if (document != null) {
            if (event.getUserSettingsFile() != DEFAULT_USER_SETTINGS_FILE) {
                log.warn("Custom settings file configured via command-line as well as via document; document taking precedence");
            }

            log.info("Using settings document ID: {}", document.getId());
            log.trace("Content:\n{}", document.getContent()); // FIXME: May contain sensitive data

            event.setUserSettingsFile(null);
            event.setUserSettingsSource(new StringSettingsSource(document.getContent(), document.getLocation()));
        }

        document = getCallback().getGlobalSettingsDocument();
        log.debug("Global settings document: {}", document);

        if (document != null) {
            if (event.getGlobalSettingsFile() != DEFAULT_GLOBAL_SETTINGS_FILE) {
                log.warn("Custom global settings file configured via command-line as well as via document; document taking precedence");
            }

            log.info("Using global settings document ID: {}", document.getId());
            log.trace("Content:\n{}", document.getContent()); // FIXME: May contain sensitive data

            event.setGlobalSettingsFile(null);
            event.setGlobalSettingsSource(new StringSettingsSource(document.getContent(), document.getLocation()));
        }
    }
}