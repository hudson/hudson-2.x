/*******************************************************************************
 *
 * Copyright (c) 2010, CloudBees, Inc.
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

package hudson.markup;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 * {@link Descriptor} for {@link MarkupFormatter}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.391
 */
public abstract class MarkupFormatterDescriptor extends Descriptor<MarkupFormatter> {
    /**
     * Returns all the registered {@link MarkupFormatterDescriptor}s.
     */
    public static DescriptorExtensionList<MarkupFormatter,MarkupFormatterDescriptor> all() {
        return Hudson.getInstance().getDescriptorList(MarkupFormatter.class);
    }
}
