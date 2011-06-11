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

package org.hudsonci.rest.client;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

/**
 * Options for opening connections.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class OpenOptions
    implements Cloneable
{
    public static final int NO_RETRIES = 0;

    public static final int UNLIMITED_RETRIES = -1;

    public static final int NO_TIMEOUT = 0;

    /**
     * The number of times to retry.
     */
    private int retries = NO_RETRIES;

    /**
     * The time to wait between retries; in seconds.
     */
    private int retryDelay = 1;

    /**
     * The time to wait before aborting; in seconds.
     */
    private int timeout = NO_TIMEOUT;

    private String username;

    private String password;

    private boolean followRedirects;

    private boolean disableCertificateValidation;

    private String proxyHost;

    private int proxyPort;

    private String proxyProtocol;

    private String proxyUsername;

    private String proxyPassword;

    public static enum Encoding
    {
        XML(APPLICATION_XML_TYPE),

        JSON(APPLICATION_JSON_TYPE),

        DEFAULT(APPLICATION_JSON_TYPE, APPLICATION_XML_TYPE);

        private final List<MediaType> accept;

        private Encoding(final MediaType... accept) {
            this.accept = Collections.unmodifiableList(Arrays.asList(accept));
        }

        public List<MediaType> getAccept() {
            return accept;
        }
    }

    private Encoding encoding;

    public int getRetries() {
        return retries;
    }

    public OpenOptions setRetries(final int retries) {
        this.retries = retries;
        return this;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public OpenOptions setRetryDelay(final int retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public OpenOptions setTimeout(final int timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    //
    // TODO: Maybe expose a general set of properties to allow for other strange configuration bits... ?
    //
    
    public boolean isDisableCertificateValidation() {
        return disableCertificateValidation;
    }

    public void setDisableCertificateValidation(final boolean disabled) {
        this.disableCertificateValidation = disabled;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(final boolean follow) {
        this.followRedirects = follow;
    }

    public Encoding getEncoding() {
        return encoding != null ? encoding : Encoding.DEFAULT;
    }

    public void setEncoding(final Encoding encoding) {
        this.encoding = encoding;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyProtocol() {
        return proxyProtocol;
    }

    public void setProxyProtocol(final String proxyProtocol) {
        this.proxyProtocol = proxyProtocol;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public String toString() {
        return "OpenOptions{" +
            "retries=" + retries +
            ", retryDelay=" + retryDelay +
            ", timeout=" + timeout +
            ", username='" + username + '\'' +
            ", password='" + (password != null ? "********" : null) + '\'' +
            ", followRedirects=" + followRedirects +
            ", disableCertificateValidation=" + disableCertificateValidation +
            ", proxyHost='" + proxyHost + '\'' +
            ", proxyPort=" + proxyPort +
            ", proxyProtocol='" + proxyProtocol + '\'' +
            ", proxyUsername='" + proxyUsername + '\'' +
            ", proxyPassword='" + (proxyPassword != null ? "********" : null) + '\'' +
            ", encoding=" + encoding +
            '}';
    }

    @Override
    public OpenOptions clone() {
        try {
            return (OpenOptions) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw (Error) new InternalError().initCause(e);
        }
    }
}
