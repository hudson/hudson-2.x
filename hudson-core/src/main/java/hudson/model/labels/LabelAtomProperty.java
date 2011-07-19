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

package hudson.model.labels;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodePropertyDescriptor;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collection;
import java.util.Collections;

/**
 * Extensible property of {@link LabelAtom}.
 *
 * <p>
 * Plugins can contribute this extension point to add additional data or UI actions to {@link LabelAtom}.
 * {@link LabelAtomProperty}s show up in the configuration screen of a label, and they are persisted
 * with the {@link LabelAtom} object.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.373
 */
@ExportedBean
public class LabelAtomProperty extends AbstractDescribableImpl<LabelAtomProperty> implements ExtensionPoint {
    /**
     * Contributes {@link Action}s to the label.
     *
     * This allows properties to create additional links in the left navigation bar and
     * hook into the URL space of the label atom.
     */
    public Collection<? extends Action> getActions(LabelAtom atom) {
        return Collections.emptyList();
    }

    /**
     * Lists up all the registered {@link LabelAtomPropertyDescriptor}s in the system.
     */
    public static DescriptorExtensionList<LabelAtomProperty,LabelAtomPropertyDescriptor> all() {
        return Hudson.getInstance().<LabelAtomProperty,LabelAtomPropertyDescriptor>getDescriptorList(LabelAtomProperty.class);
    }
}
