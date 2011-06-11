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

import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.StringSettingsSource;
import org.hudsonci.maven.eventspy.common.DocumentReference;
import org.hudsonci.maven.eventspy_30.EventSpyHandler;

import javax.inject.Named;

import static org.apache.maven.cli.MavenCli.DEFAULT_GLOBAL_SETTINGS_FILE;
import static org.apache.maven.cli.MavenCli.DEFAULT_USER_SETTINGS_FILE;

/**
 * Handles {@link SettingsBuildingRequest} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
