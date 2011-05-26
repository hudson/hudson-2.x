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

package org.hudsonci.rest.client.internal;

import javax.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.hudsonci.rest.client.HandshakeException;
import org.hudsonci.rest.client.HandshakeFailedException;
import org.hudsonci.rest.client.HudsonClient;
import org.hudsonci.rest.client.HudsonClientException;
import org.hudsonci.rest.client.OpenOptions;
import org.hudsonci.rest.client.internal.jersey.JerseyClientFactory;
import org.hudsonci.rest.client.internal.ssl.TrustAllX509TrustManager;
import org.hudsonci.rest.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.rest.common.Constants.BASE_REST_PATH;
import static org.hudsonci.rest.common.Constants.HUDSON_HEADER;

/**
 * Hudson client implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class HudsonClientImpl
    implements HudsonClient
{
    private static final Logger log = LoggerFactory.getLogger(HudsonClientImpl.class);

    private final UUID id = UUID.randomUUID();

    private final JerseyClientFactory clientFactory;

    private final Map<String,Extension> extensions = new HashMap<String,Extension>();

    private URI baseUri;

    private OpenOptions options;

    private Client client;

    @Inject
    public HudsonClientImpl(final JerseyClientFactory clientFactory, final Map<String,Extension> extconfig) {
        this.clientFactory = checkNotNull(clientFactory);
        log.info("Version: {}", getVersion());

        assert extconfig != null;
        if (extconfig.isEmpty()) {
            log.error("No extensions configured");
        }
        else {
            log.debug("Extensions:");
            for (Map.Entry<String,Extension> entry : extconfig.entrySet()) {
                String key = entry.getKey();
                Extension ext = entry.getValue();
                log.debug("  {} -> {}", key, ext);
                ext.init(this);
                extensions.put(key, ext);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        }
        finally {
            super.finalize();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "uri=" + getBaseUri() +
            ", open=" + isOpen() +
            ", id=" + getId() +
            '}';
    }

    public UUID getId() {
        return id;
    }

    public String getVersion() {
        return org.hudsonci.rest.client.Version.get().getVersion();
    }

    private String getReportVersion() {
        String version = System.getProperty(HudsonClientImpl.class.getName() + ".reportVersionOverride");
        if (version != null) {
            log.warn("Report version override: {}", version);
            return version;
        }
        return getVersion();
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public void open(final URI uri) {
        assert uri != null;

        // Open with default options
        open(uri, new OpenOptions());
    }

    public void open(final URI uri, final OpenOptions options) {
        checkNotNull(uri);
        checkNotNull(options);

        if (isOpen()) {
            close();
        }

        log.debug("Opening: {}; w/options: {}", uri, options);

        // Save options
        this.options = options.clone();

        // Save this early, used by validation, could be used by ext.open() too.
        this.baseUri = uri;

        // Disable SSL certificate trust validation, so client can connect to server w/self-signed certificate
        if (options.isDisableCertificateValidation()) {
            try {
                TrustAllX509TrustManager.install();
            }
            catch (Exception e) {
                log.error("Failed to install 'trust-all' trust manager", e);
            }
        }

        // Create a new REST-client instance
        this.client = clientFactory.create(options);

        // Tell all extensions we are opening; this is a a wee-bit of life-cycle notifications
        for (Extension ext : extensions.values()) {
            try {
                ext.open();
            }
            catch (Exception e) {
                throw new HudsonClientException(String.format("Failed to open extension: %s", ext.getClass().getName()), e);
            }
        }

        handshake(uri, client, options);

        // If we get this far then things should be okay
        log.debug("Opened");
    }

    public OpenOptions getOptions() {
        return options;
    }

    private void handshake(final URI uri, final Client client, final OpenOptions options) {
        assert uri != null;
        assert client != null;
        assert options != null;

        log.debug("Handshaking");

        final Timer timer = new Timer(true);
        final AtomicBoolean timedOut = new AtomicBoolean(false);

        // Schedule a timeout if configured
        if (options.getTimeout() > OpenOptions.NO_TIMEOUT) {
            TimerTask task = new TimerTask()
            {
                @Override
                public void run() {
                    log.debug("Task has timed-out");
                    timedOut.set(true);
                }
            };

            timer.schedule(task, options.getTimeout() * 1000);
        }

        //
        // Possible states:
        //
        //  1 - Can't connect (ie. server is down) -> RETRY
        //  2 - Connected, but returns garbage back (REST is probably not loaded yet), or other exception while fetching resource -> RETRY
        //  3 - Connected, REST is functional, but returns invalid response (not a hudson server, or its crazy) -> ERROR
        //  4 - Connected, Rest is functional and response is valid -> OK
        //

        List<Throwable> failures = new LinkedList<Throwable>();
        boolean valid = false;
        int count = 0;

        while (!valid) {
            if (timedOut.get()) {
                throw new HandshakeException("Handshake timed-out");
            }
            if (Thread.interrupted()) {
                throw new HandshakeException("Handshake interrupted");
            }

            count++;
            log.debug("Handshake attempt: {}", count);

            ClientResponse resp = null;
            try {
                resp = resource(client, uri().path("handshake")).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

                // We need a 200/OK, anything else is bad
                ResponseUtil.ensureStatus(resp, Response.Status.OK); // -> RETRY

                // See if the server has returned us our ID token, if not, something is wrong
                String token = resp.getEntity(String.class);
                if (!id.toString().equals(token)) {
                    throw new HandshakeException(String.format("Invalid handshake response data: %s", token)); // -> RETRY or ERROR ?
                }

                // Should be okay now, lets continue
                valid = true;
            }
            catch (Exception e) {
                log.debug("Handshake failure", e);
                failures.add(e);

                // Give up if we are counting retries after we reached the limit
                if (options.getRetries() > OpenOptions.UNLIMITED_RETRIES && count > options.getRetries()) {
                    throw new HandshakeFailedException(String.format("Failed to handshake after %d attempts", count), failures);
                }

                // Wait, then continue
                try {
                    Thread.sleep(options.getRetryDelay() * 1000);
                }
                catch (InterruptedException x) {
                    throw new HandshakeException("Handshake interrupted");
                }
            }
            finally {
                ResponseUtil.close(resp);
            }
        }

        // handshake is complete
        timer.cancel();
    }

    public boolean isOpen() {
        return client != null;
    }

    public void ensureOpened() {
        if (!isOpen()) {
            throw new IllegalStateException("Not opened");
        }
    }

    public void close() {
        if (isOpen()) {
            log.debug("Closing");

            // First try to close extensions
            for (Extension ext : extensions.values()) {
                try {
                    ext.close();
                }
                catch (Exception e) {
                    log.warn("Failed to close extension: {}; ignoring", ext.getClass().getName(), e);
                }
            }
            client.destroy();

            // Then we are done, be one with the null
            client = null;
        }
    }

    public Client getClient() {
        ensureOpened();

        return client;
    }

    private String hudsonHeader;

    private String getHudsonHeader() {
        if (hudsonHeader == null) {
            hudsonHeader = String.format("client=%s;id=%s", getReportVersion(), getId());
        }
        return hudsonHeader;
    }

    /**
     * Exposed to allow {@link #handshake} to re-use the basic resource construction logic,
     * since at that point the underlying client has not been preserved.
     *
     * Automatically includes {@link Constants#HUDSON_HEADER} header.
     */
    private WebResource.Builder resource(final Client client, final URI uri) {
        assert client != null;
        assert uri != null;

        WebResource resource = client.resource(uri);
        WebResource.Builder builder = resource.getRequestBuilder();
        return builder.header(HUDSON_HEADER, getHudsonHeader());
    }

    private WebResource.Builder resource(final Client client, final UriBuilder uri) {
        assert client != null;
        assert uri != null;

        return resource(client, uri.build());
    }

    public WebResource.Builder resource(final URI uri) {
        return resource(getClient(), uri);
    }

    public WebResource.Builder resource(final UriBuilder uri) {
        return resource(uri.build());
    }

    public UriBuilder uri() {
        return UriBuilder.fromUri(getBaseUri()).path(BASE_REST_PATH);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Extension> T ext(final Class<T> type) {
        checkNotNull(type);
        T ext = (T) extensions.get(type.getName());
        if (ext == null) {
            throw new IllegalArgumentException("Unknown client extension type: " + type);
        }
        return ext;
    }
}
