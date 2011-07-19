/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
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

package org.eclipse.hudson.legacy.maven.plugin.reporters;

import hudson.model.Action;
import org.apache.maven.reporting.MavenReport;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * {@link Action} to display links to the generated {@link MavenReport Maven reports}.
 * @author Kohsuke Kawaguchi
 */
public final class ReportAction implements Action, Serializable {

    private final List<Entry> entries = new ArrayList<Entry>();

    public static final class Entry {
        /**
         * Relative path to the top of the report withtin the project reporting directory.
         */
        public final String path;
        public final String title;

        public Entry(String path, String title) {
            this.path = path;
            this.title = title;
        }
    }

    public ReportAction() {
    }

    protected void add(Entry e) {
        entries.add(e);
    }

    public String getIconFileName() {
        // TODO
        return "n/a.gif";
    }

    public String getDisplayName() {
        return Messages.ReportAction_DisplayName();
    }

    public String getUrlName() {
        return "mavenReports";
    }

    private static final long serialVersionUID = 1L;
}
