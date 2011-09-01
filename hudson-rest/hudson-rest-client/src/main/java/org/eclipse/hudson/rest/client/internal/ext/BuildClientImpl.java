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

package org.eclipse.hudson.rest.client.internal.ext;

import com.google.common.base.Preconditions;
import javax.inject.Inject;
import org.eclipse.hudson.rest.model.build.BuildDTO;
import org.eclipse.hudson.rest.model.build.BuildEventDTO;
import org.eclipse.hudson.rest.model.build.BuildsDTO;
import org.eclipse.hudson.rest.model.build.ChangesDTO;
import org.eclipse.hudson.rest.model.build.ConsoleDTO;
import org.eclipse.hudson.rest.model.build.TestsDTO;
import com.sun.jersey.api.client.ClientResponse;
import org.cometd.Client;
import org.cometd.Message;
import org.cometd.MessageListener;
import org.cometd.client.BayeuxClient;
import org.eclipse.hudson.rest.client.HudsonClientException;
import org.eclipse.hudson.rest.client.ext.BuildClient;
import org.eclipse.hudson.rest.client.ext.NotificationClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.rest.common.JsonCodec;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.utils.common.Varargs.$;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * {@link BuildClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BuildClientImpl
    extends HudsonClientExtensionSupport
    implements BuildClient
{
    private final JsonCodec json;

    @Inject
    public BuildClientImpl(final JsonCodec json) {
        this.json = checkNotNull(json);
    }

    @Override
    public void close() throws Exception {
        if (eventBroadcastListener != null) {
            bayeux().removeListener(eventBroadcastListener);
        }
    }

    protected UriBuilder uri() {
        return getClient().uri().path("projects");
    }

    private UriBuilder projectUri(final String projectName) {
        assert projectName != null;
        return uri().path(encodeProjectName(projectName));
    }

    public List<BuildDTO> getBuilds(final String projectName) {
        ClientResponse resp = resource(projectUri(projectName).path("builds")).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK, NO_CONTENT);
            if (isStatus(resp, NO_CONTENT)) {
                return Collections.emptyList();
            }
            return resp.getEntity(BuildsDTO.class).getBuilds();
        }
        finally {
            close(resp);
        }
    }

    private UriBuilder buildUri(final String projectName, final int buildNumber) {
        Preconditions.checkNotNull(projectName);
        Preconditions.checkArgument(buildNumber > 0, "build number must be greater than zero");
        return projectUri(projectName).path(String.valueOf(buildNumber));
    }

    public BuildDTO getBuild(final String projectName, final int buildNumber) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber)).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(BuildDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public void stopBuild(final String projectName, final int buildNumber) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber).path("stop")).put(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
        }
        finally {
            close(resp);
        }
    }

    public void keepBuild(final String projectName, final int buildNumber, final boolean release) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber).queryParam("release", release).path("keep"))
            .get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public void deleteBuild(final String projectName, final int buildNumber) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber)).delete(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
        }
        finally {
            close(resp);
        }
    }

    public ChangesDTO getChanges(final String projectName, final int buildNumber) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber).path("changes")).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK, NO_CONTENT);
            if (isStatus(resp, NO_CONTENT)) {
                return new ChangesDTO();
            }
            return resp.getEntity(ChangesDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public TestsDTO getTests(final String projectName, final int buildNumber) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber).path("tests")).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK, NO_CONTENT);
            if (isStatus(resp, NO_CONTENT)) {
                return new TestsDTO();
            }
            return resp.getEntity(TestsDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public ConsoleDTO getConsole(final String projectName, final int buildNumber) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber).path("console")).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(ConsoleDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public InputStream getConsoleContent(final String projectName, final int buildNumber, final long offset, final long length) {
        ClientResponse resp = resource(buildUri(projectName, buildNumber).queryParam("offset", offset).queryParam("length", length)
                .path("console").path("content"))
            .accept(TEXT_PLAIN).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(InputStream.class);
        }
        finally {
            close(resp);
        }
    }

    //
    // Event support
    //

    // FIXME: This is a work around and should be replaced with something more sane.

    private static final String SUBSCRIBE_CHANNEL = "/meta/subscribe";

    private static final String BUILDS_CHANNEL = "/builds";

    private final List<BuildListener> listeners = new ArrayList<BuildListener>();

    private EventBroadcastListener eventBroadcastListener;

    /**
     * Flag set for build notification subscription status.  Also used as wait lock when subscribing.
     */
    private final AtomicBoolean buildsChannelSubscribed = new AtomicBoolean(false);

    private class EventBroadcastListener
        implements MessageListener
    {
        public void deliver(final Client from, final Client to, final Message msg) {
            assert msg != null;

            String channel = msg.getChannel();
            if (BUILDS_CHANNEL.equals(channel)) {
                fireBuildEvent(msg);
            }
            else if (SUBSCRIBE_CHANNEL.equals(channel)) {
                String subscribedChannel = (String) msg.get("subscription");
                log.debug("Got subscription response for channel: {}", subscribedChannel);

                // ATM this assumes that we are not subscribed at this point (but who knows...)
                if (BUILDS_CHANNEL.equals(subscribedChannel)) {
                    boolean success = (Boolean)msg.get("successful");
                    synchronized (buildsChannelSubscribed) {
                        log.debug("Notifying subscription to build notifications: {}", success);
                        buildsChannelSubscribed.set(success);
                        buildsChannelSubscribed.notifyAll();
                    }
                }
            }
            else {
                log.debug("Ignoring message: {} -> {} = {}", $(from, to, msg));
            }
        }
    }

    public void addBuildListener(final BuildListener listener) {
        assert listener != null;

        synchronized (listeners) {
            listeners.add(listener);
            updateSubscription();
        }

        log.debug("Added build listener: {}", listener);
    }

    public void removeBuildListener(final BuildListener listener) {
        assert listener != null;

        synchronized (listeners) {
            listeners.remove(listener);
            updateSubscription();
        }

        log.debug("Removed build listener: {}", listener);
    }

    private BayeuxClient bayeux() {
        return getClient().ext(NotificationClient.class).getBayeuxClient();
    }

    private void updateSubscription() {
        BayeuxClient bayeuxClient = bayeux();

        // Find out if we are already subscribed
        boolean subscribed = buildsChannelSubscribed.get();

        // If subscribed and no listeners, then unsubscribe
        if (subscribed && listeners.isEmpty()) {
            bayeuxClient.unsubscribe(BUILDS_CHANNEL);
            bayeuxClient.removeListener(eventBroadcastListener);
            buildsChannelSubscribed.set(false);
            // no real need to wait for a response here?

            log.debug("Removed event broadcaster");
        }
        // If not yet subscribed and we have listeners, then subscribe
        else if (!subscribed && listeners.size() >= 1) {
            if (eventBroadcastListener == null) {
                eventBroadcastListener = new EventBroadcastListener();
            }

            bayeuxClient.addListener(eventBroadcastListener);
            bayeuxClient.subscribe(BUILDS_CHANNEL);

            // We have to wait for an ack of the subscription, or else users will go on thinking they will get events,
            // which won't happen until the subscription is active on the server.

            log.debug("Waiting for subscription confirmation");
            synchronized (buildsChannelSubscribed) {
                try {
                    // wait 5 min then give up (probably too long, but whatever)
                    buildsChannelSubscribed.wait(5 * 60 * 1000);
                    if (!buildsChannelSubscribed.get()) {
                        throw new RuntimeException("Failed to receive subscription confirmation in time; giving up");
                    }
                }
                catch (InterruptedException e) {
                    log.error("Interrupted while waiting for subscription confirmation", e);
                }
            }

            log.debug("Added event broadcaster");
        }
        // else broadcaster already configured
    }

    private void fireBuildEvent(final Message msg) {
        assert msg != null;

        BuildListener[] targets;
        synchronized (listeners) {
            targets = listeners.toArray(new BuildListener[listeners.size()]);
        }

        String data = (String) msg.getData();
        BuildEventDTO event;
        try {
            event = json.decode(data, BuildEventDTO.class);
        }
        catch (IOException e) {
            throw new HudsonClientException("Failed to decode event data", e);
        }

        for (BuildListener listener : targets) {
            log.debug("Invoking listener: {}", listener);

            switch (event.getType()) {
                case STARTED:
                    listener.buildStarted(event);
                    break;

                case STOPPED:
                    listener.buildStopped(event);
                    break;
            }
        }
    }
}
