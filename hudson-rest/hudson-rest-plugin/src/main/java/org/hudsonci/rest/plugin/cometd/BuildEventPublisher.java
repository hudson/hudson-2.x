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

package org.hudsonci.rest.plugin.cometd;

import org.hudsonci.rest.common.JsonCodec;
import org.hudsonci.rest.model.build.BuildEventDTO;
import org.hudsonci.rest.model.build.BuildEventTypeDTO;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.cometd.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.rest.model.build.BuildEventTypeDTO.STARTED;
import static org.hudsonci.rest.model.build.BuildEventTypeDTO.STOPPED;

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
