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

package hudson.tasks._ant;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleNote;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;

import java.util.regex.Pattern;

/**
 * Marks the log line "TARGET:" that Ant uses to mark the beginning of the new target.
 * @sine 1.349
 */
public final class AntTargetNote extends ConsoleNote {
    public AntTargetNote() {
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        // still under development. too early to put into production
        if (!ENABLED)   return null;

        MarkupText.SubText t = text.findToken(Pattern.compile(".*(?=:)"));
        if (t!=null)
            t.addMarkup(0,t.length(),"<b class=ant-target>","</b>");
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "Ant targets";
        }
    }

    public static boolean ENABLED = !Boolean.getBoolean(AntTargetNote.class.getName()+".disabled");
}
