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

package org.eclipse.hudson.service.internal;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;

/**
 * A dummy {@link StaplerResponse}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DummyStaplerResponse
    implements StaplerResponse
{
    public static final DummyStaplerResponse INSTANCE = new DummyStaplerResponse();
    
    public void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException {
    }

    public void forwardToPreviousPage(StaplerRequest request) throws ServletException, IOException {
    }

    public void sendRedirect2(String url) throws IOException {
    }

    public void serveFile(StaplerRequest request, URL res) throws ServletException, IOException {
    }

    public void serveFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException {
    }

    public void serveLocalizedFile(StaplerRequest request, URL res) throws ServletException, IOException {
    }

    public void serveLocalizedFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException {
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, long contentLength, String fileName) throws
        ServletException, IOException
    {
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, int contentLength, String fileName) throws
        ServletException, IOException
    {
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long contentLength, String fileName) throws
        ServletException, IOException
    {
    }

    public void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName) throws
        ServletException, IOException
    {
    }

    public void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor) throws ServletException, IOException {
    }

    public OutputStream getCompressedOutputStream(HttpServletRequest req) throws IOException {
        return null;
    }

    public Writer getCompressedWriter(HttpServletRequest req) throws IOException {
        return null;
    }

    public int reverseProxyTo(URL url, StaplerRequest req) throws IOException {
        return 0;
    }

    public void addCookie(Cookie cookie) {
    }

    public boolean containsHeader(String name) {
        return false;
    }

    public String encodeURL(String url) {
        return null;
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeUrl(String url) {
        return null;
    }

    public String encodeRedirectUrl(String url) {
        return null;
    }

    public void sendError(int sc, String msg) throws IOException {
    }

    public void sendError(int sc) throws IOException {
    }

    public void sendRedirect(String location) throws IOException {
    }

    public void setDateHeader(String name, long date) {
    }

    public void addDateHeader(String name, long date) {
    }

    public void setHeader(String name, String value) {
    }

    public void addHeader(String name, String value) {
    }

    public void setIntHeader(String name, int value) {
    }

    public void addIntHeader(String name, int value) {
    }

    public void setStatus(int sc) {
    }

    public void setStatus(int sc, String sm) {
    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public void setCharacterEncoding(String charset) {
    }

    public void setContentLength(int len) {
    }

    public void setContentType(String type) {
    }

    public void setBufferSize(int size) {
    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {
    }

    public void resetBuffer() {
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void setLocale(Locale loc) {
    }

    public Locale getLocale() {
        return null;
    }
}
