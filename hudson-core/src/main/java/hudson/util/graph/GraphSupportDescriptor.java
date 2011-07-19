/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *      
 *
 *******************************************************************************/ 

package hudson.util.graph;

import hudson.model.Descriptor;

/**
 * {@link Descriptor} for {@link GraphSupport}.
 *
 * @author Winston Prakash
 * @since 2.0.1
 */
public abstract class GraphSupportDescriptor extends Descriptor<GraphSupport> {
    // so far nothing different from plain Descriptor
    // but it may prove useful for future expansion
}
