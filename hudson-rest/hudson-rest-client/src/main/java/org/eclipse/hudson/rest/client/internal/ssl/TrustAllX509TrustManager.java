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

package org.eclipse.hudson.rest.client.internal.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Trust manager which trusts everything, allowing for self-signed SSL certificates to be used.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class TrustAllX509TrustManager
    implements X509TrustManager
{
    private static final Logger log = LoggerFactory.getLogger(TrustAllX509TrustManager.class);

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
        // empty
    }

    public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
        // empty
    }

    public static synchronized void install() throws KeyManagementException, NoSuchAlgorithmException {
        // FIXME: Install for a specific thread and its children, not for the entire JVM?

        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[] { new TrustAllX509TrustManager() }, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        log.warn("Trust-all SSL trust manager installed");
    }
}
