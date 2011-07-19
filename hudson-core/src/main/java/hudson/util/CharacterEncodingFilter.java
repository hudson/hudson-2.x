/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Seiji Sogabe
 *      
 *
 *******************************************************************************/ 

package hudson.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter that sets the character encoding to be used in parsing the request
 * to avoid Non-ASCII characters garbled.
 *
 * @author Seiji Sogabe
 */
public class CharacterEncodingFilter implements Filter {

    /**
     * The default character encoding.
     */
    private static final String ENCODING = "UTF-8";

    private static final Boolean DISABLE_FILTER
            = Boolean.getBoolean(CharacterEncodingFilter.class.getName() + ".disableFilter");

    /**
     * The character encoding sets forcibly?
     */
    private static final Boolean FORCE_ENCODING
            = Boolean.getBoolean(CharacterEncodingFilter.class.getName() + ".forceEncoding");

    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO,
                "CharacterEncodingFilter initialized. DISABLE_FILTER: {0} FORCE_ENCODING: {1}",
                new Object[]{DISABLE_FILTER, FORCE_ENCODING});
    }

    public void destroy() {
        LOGGER.info("CharacterEncodingFilter destroyed.");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!DISABLE_FILTER) {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) request;
                if (shouldSetCharacterEncoding(req)) {
                    req.setCharacterEncoding(ENCODING);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSetCharacterEncoding(HttpServletRequest req) {
        String method = req.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }

        // containers often implement RFCs incorrectly in that it doesn't interpret query parameter
        // decoding with UTF-8. This will ensure we get it right.
        // but doing this for config.xml submission could potentiall overwrite valid
        // "text/xml;charset=xxx"
        String contentType = req.getContentType();
        if (contentType != null) {
            boolean isXmlSubmission = contentType.startsWith("application/xml") || contentType.startsWith("text/xml");
            if (isXmlSubmission) {
                return false;
            }
        }

        if (FORCE_ENCODING || req.getCharacterEncoding() == null) {
            return true;
        }
        
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(CharacterEncodingFilter.class.getName());
}
