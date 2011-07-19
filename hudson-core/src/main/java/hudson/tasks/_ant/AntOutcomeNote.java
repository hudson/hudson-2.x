/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc..
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

package hudson.tasks._ant;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

/**
 * Annotates the BUILD SUCCESSFUL/FAILED line of the Ant execution.
 *
 * @author Kohsuke Kawaguchi
 */
public class AntOutcomeNote extends ConsoleNote {
    public AntOutcomeNote() {
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        if (text.getText().contains("FAIL"))
            text.addMarkup(0,text.length(),"<span class=ant-outcome-failure>","</span>");
        if (text.getText().contains("SUCCESS"))
            text.addMarkup(0,text.length(),"<span class=ant-outcome-success>","</span>");
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "Ant build outcome";
        }
    }
}
