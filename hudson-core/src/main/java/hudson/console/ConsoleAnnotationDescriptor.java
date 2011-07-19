/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package hudson.console;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.TimeUnit2;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;

/**
 * Descriptor for {@link ConsoleNote}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.349
 */
public abstract class ConsoleAnnotationDescriptor extends Descriptor<ConsoleNote<?>> implements ExtensionPoint {
    public ConsoleAnnotationDescriptor(Class<? extends ConsoleNote<?>> clazz) {
        super(clazz);
    }

    public ConsoleAnnotationDescriptor() {
    }

    /**
     * {@inheritDoc}
     *
     * Users use this name to enable/disable annotations.
     */
    public abstract String getDisplayName();

    /**
     * Returns true if this descriptor has a JavaScript to be inserted on applicable console page.
     */
    public boolean hasScript() {
        return hasResource("/script.js") !=null;
    }

    /**
     * Returns true if this descriptor has a stylesheet to be inserted on applicable console page.
     */
    public boolean hasStylesheet() {
        return hasResource("/style.css") !=null;
    }

    private URL hasResource(String name) {
        return clazz.getClassLoader().getResource(clazz.getName().replace('.','/').replace('$','/')+ name);
    }

    @WebMethod(name="script.js")
    public void doScriptJs(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.serveFile(req, hasResource("/script.js"), TimeUnit2.DAYS.toMillis(1));
    }

    @WebMethod(name="style.css")
    public void doStyleCss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.serveFile(req, hasResource("/style.css"), TimeUnit2.DAYS.toMillis(1));
    }

    /**
     * Returns all the registered {@link ConsoleAnnotationDescriptor} descriptors.
     */
    public static DescriptorExtensionList<ConsoleNote<?>,ConsoleAnnotationDescriptor> all() {
        return (DescriptorExtensionList)Hudson.getInstance().getDescriptorList(ConsoleNote.class);
    }
}
