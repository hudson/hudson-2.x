/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package hudson.model;

import hudson.EnvVars;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.Stapler;

import javax.servlet.ServletException;
import java.io.IOException;

import hudson.search.SearchableModelObject;
import hudson.search.Search;
import hudson.search.SearchIndexBuilder;
import hudson.search.SearchIndex;

/**
 * {@link ModelObject} with some convenience methods.
 * 
 * @author Kohsuke Kawaguchi
 * @author Nikita Levyankov
 */
public abstract class AbstractModelObject implements SearchableModelObject {
    /**
     * Displays the error in a page.
     */
    protected final void sendError(Exception e, StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
        sendError(e.getMessage(),req,rsp);
    }

    protected final void sendError(Exception e) throws ServletException, IOException {
        sendError(e,Stapler.getCurrentRequest(),Stapler.getCurrentResponse());
    }

    protected final void sendError(String message, StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
        req.setAttribute("message",message);
        rsp.forward(this,"error",req);
    }

    /**
     * @param pre
     *      If true, the message is put in a PRE tag.
     */
    protected final void sendError(String message, StaplerRequest req, StaplerResponse rsp, boolean pre) throws ServletException, IOException {
        req.setAttribute("message",message);
        if(pre)
            req.setAttribute("pre",true);
        rsp.forward(this,"error",req);
    }

    protected final void sendError(String message) throws ServletException, IOException {
        sendError(message,Stapler.getCurrentRequest(),Stapler.getCurrentResponse());
    }

    /**
     * Convenience method to verify that the current request is a POST request.
     */
    protected final void requirePOST() throws ServletException {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req==null)  return; // invoked outside the context of servlet
        String method = req.getMethod();
        if(!method.equalsIgnoreCase("POST"))
            throw new ServletException("Must be POST, Can't be "+method);
    }

    /**
     * Checks jndi,environment, hudson environment and system properties for specified key.
     * Property is checked in direct order:
     * <ol>
     * <li>JNDI ({@link InitialContext#lookup(String)})</li>
     * <li>Hudson environment ({@link EnvVars#masterEnvVars})</li>
     * <li>System properties ({@link System#getProperty(String)})</li>
     * </ol>
     * @param key - the name of the configured property.
     * @return the string value of the configured property, or null if there is no property with that key.
     */
    protected String getConfiguredHudsonProperty(String key) {
        if (StringUtils.isNotBlank(key)) {
            String resultValue;
            try {
                InitialContext iniCtxt = new InitialContext();
                Context env = (Context) iniCtxt.lookup("java:comp/env");
                resultValue = StringUtils.trimToNull((String) env.lookup(key));
                if (null != resultValue) {
                    return resultValue;
                }
                // look at one more place. See http://issues.hudson-ci.org/browse/HUDSON-1314
                resultValue = StringUtils.trimToNull((String) iniCtxt.lookup(key));
                if (null != resultValue) {
                    return resultValue;
                }
            } catch (NamingException e) {
                // ignore
            }

            // look at the env var next
            resultValue = StringUtils.trimToNull(EnvVars.masterEnvVars.get(key));
            if (null != resultValue) {
                return resultValue;
            }

            // finally check the system property
            resultValue = StringUtils.trimToNull(System.getProperty(key));
            if (null != resultValue) {
                return resultValue;
            }
        }
        return null;
    }

    /**
     * Default implementation that returns empty index.
     */
    protected SearchIndexBuilder makeSearchIndex() {
        return new SearchIndexBuilder().addAllAnnotations(this);
    }

    public final SearchIndex getSearchIndex() {
        return makeSearchIndex().make();
    }

    public Search getSearch() {
        return new Search();
    }

    /**
     * Default implementation that returns the display name.
     */
    public String getSearchName() {
        return getDisplayName();
    }
}
