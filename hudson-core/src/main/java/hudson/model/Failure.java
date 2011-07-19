/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Represents an error induced by user, encountered during HTTP request processing.
 *
 * <p>
 * The error page is rendered into HTML, but without a stack trace. So only use
 * this exception when the error condition is anticipated by the program, and where
 * we nor users don't need to see the stack trace to figure out the root cause. 
 *
 * @author Kohsuke Kawaguchi
 * @since 1.321
 */
public class Failure extends RuntimeException implements HttpResponse {
    private final boolean pre;

    public Failure(String message) {
        this(message,false);
    }

    public Failure(String message, boolean pre) {
        super(message);
        this.pre = pre;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        req.setAttribute("message",getMessage());
        if(pre)
            req.setAttribute("pre",true);
        if (node instanceof AbstractItem) // Maintain ancestors
            rsp.forward(Hudson.getInstance(), ((AbstractItem)node).getUrl() + "error", req);
        else
            rsp.forward(node instanceof AbstractModelObject ? node : Hudson.getInstance() ,"error", req);
    }
}
