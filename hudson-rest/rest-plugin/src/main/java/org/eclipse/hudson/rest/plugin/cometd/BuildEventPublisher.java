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

package org.eclipse.hudson.rest.plugin.cometd;

import org.eclipse.hudson.rest.model.build.BuildEventDTO;
import org.eclipse.hudson.rest.model.build.BuildEventTypeDTO;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.cometd.Channel;
import org.eclipse.hudson.rest.common.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.rest.model.build.BuildEventTypeDTO.STARTED;
import static org.eclipse.hudson.rest.model.build.BuildEventTypeDTO.STOPPED;

/**
 * Publishes {@link BuildEventDTO} messages.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class BuildEventPublisher
    extends RunListener<AbstractBuild>
{
    private static final String CHANNEL = "/builds";

    private static final Logger log = LoggerFactory.getLogger(BuildEventPublisher.class);

    private final JsonCodec codec;

    @Inject
    public BuildEventPublisher(final JsonCodec codec) {
        super(AbstractBuild.class);
        this.codec = checkNotNull(codec);
    }

    @Override
    public void onStarted(final AbstractBuild build, final TaskListener listener) {
        publishEvent(createEvent(build, STARTED));
    }

    @Override
    public void onFinalized(final AbstractBuild build) {
        checkNotNull(build);
        publishEvent(createEvent(build, STOPPED));
    }

    private BuildEventDTO createEvent(final AbstractBuild build, final BuildEventTypeDTO type) {
        assert build != null;
        assert type != null;

        return new BuildEventDTO()
            .withType(type)
            .withProjectName(build.getParent().getFullName())
            .withBuildNumber(build.getNumber());
    }

    private void publishEvent(final BuildEventDTO event) {
        assert event != null;
        try {
            // TODO: Use String.format("%s/%s", CANNEL, event.getProjectName()) though will probably have to encode the name
            Channel channel = CometdProvider.getChannel(CHANNEL, false);
            if (channel != null) {
                String data = codec.encode(event);
                log.debug("Publishing event w/data: {}", data);
                channel.publish(null, data, null);
            }
            else {
                log.trace("Channel does not exist; skipping publish event");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish event", e);
        }
    }
}
