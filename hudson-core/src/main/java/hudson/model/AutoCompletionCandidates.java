/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model;

import hudson.search.Search;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data representation of the auto-completion candidates.
 * <p>
 * This object should be returned from your doAutoCompleteXYZ methods.
 *
 * @author Kohsuke Kawaguchi
 */
public class AutoCompletionCandidates implements HttpResponse {
    private final List<String> values = new ArrayList<String>();

    public AutoCompletionCandidates add(String v) {
        values.add(v);
        return this;
    }

    public AutoCompletionCandidates add(String... v) {
        values.addAll(Arrays.asList(v));
        return this;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object o) throws IOException, ServletException {
        Search.Result r = new Search.Result();
        for (String value : values) {
            r.suggestions.add(new hudson.search.Search.Item(value));
        }
        rsp.serveExposedBean(req,r, Flavor.JSON);
    }
}
