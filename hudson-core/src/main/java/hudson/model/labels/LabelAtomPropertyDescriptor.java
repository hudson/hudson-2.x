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

import hudson.Extension;
import hudson.model.Descriptor;

/**
 * {@link Descriptor} for {@link LabelAtom}.
 *
 * <p>
 * Put {@link Extension} on your descriptor implementation to have it auto-registered.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.373
 */
public abstract class LabelAtomPropertyDescriptor extends Descriptor<LabelAtomProperty> {

}
