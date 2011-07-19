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

package hudson.tasks._maven;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

import java.util.regex.Pattern;

/**
 * Marks the log line that reports that Maven is executing a mojo.
 * It'll look something like this:
 *
 * <pre>[INFO] [pmd:pmd {execution: default}]</pre>
 *
 * @author Kohsuke Kawaguchi
 */
public class MavenMojoNote extends ConsoleNote {
    public MavenMojoNote() {
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        text.addMarkup(7,text.length(),"<b class=maven-mojo>","</b>");
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "Maven Mojos";
        }
    }

    public static Pattern PATTERN = Pattern.compile("\\[INFO\\] \\[[A-Za-z0-9-_]+:[A-Za-z0-9-_]+ \\{execution: [A-Za-z0-9-_]+\\}\\]");
}
