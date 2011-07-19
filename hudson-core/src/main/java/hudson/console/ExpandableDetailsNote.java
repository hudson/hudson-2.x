/*******************************************************************************
 *
 * Copyright (c) 2010-2011, CloudBees, Inc.
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

import hudson.Extension;
import hudson.MarkupText;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders a button that can be clicked to reveal additional block tag (and HTML inside it.)
 *
 * <p>
 * Useful if you want the user to be able to see additional details.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.395
 */
public class ExpandableDetailsNote extends ConsoleNote {
    private final String caption;
    private final String html;

    public ExpandableDetailsNote(String caption, String html) {
        this.caption = caption;
        this.html = html;
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        text.addMarkup(charPos,
                "<input type=button value='"+caption+"' class='reveal-expandable-detail'><div class='expandable-detail'>"+html+"</div>");
        return null;
    }

    public static String encodeTo(String buttonCaption, String html) {
        try {
            return new ExpandableDetailsNote(buttonCaption, html).encode();
        } catch (IOException e) {
            // impossible, but don't make this a fatal problem
            LOGGER.log(Level.WARNING, "Failed to serialize "+HyperlinkNote.class,e);
            return "";
        }
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "Expandable details";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ExpandableDetailsNote.class.getName());
}
