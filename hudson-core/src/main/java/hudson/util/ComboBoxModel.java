/*******************************************************************************
 *
 * Copyright (c) 2004-2011, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    CollabNet
 *        
 *
 *******************************************************************************/ 

package hudson.util;

import net.sf.json.JSONArray;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Model object for dynamically filed combo box, which is really just {@code ArrayList<String>}
 * 
 * @author Kohsuke Kawaguchi
 */
public class ComboBoxModel extends ArrayList<String> implements HttpResponse {
    public ComboBoxModel(int initialCapacity) {
        super(initialCapacity);
    }

    public ComboBoxModel() {
    }

    public ComboBoxModel(Collection<? extends String> c) {
        super(c);
    }

    public ComboBoxModel(String... values) {
        this(asList(values));
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setContentType(Flavor.JSON.contentType);
        PrintWriter w = rsp.getWriter();
        w.print('(');
        JSONArray.fromObject(this).write(w);
        w.print(')');
    }
}
