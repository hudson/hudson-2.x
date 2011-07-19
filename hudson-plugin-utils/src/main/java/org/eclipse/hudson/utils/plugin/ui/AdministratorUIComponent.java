/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.utils.plugin.ui;

import hudson.model.Action;
import hudson.model.Hudson;
import hudson.security.Permission;

/**
 * UI components for administrative activities.
 * 
 * The user must be an administrator to view and optionally to submit forms.
 * 
 * Subclasses should call the {@link #checkPermission()} before performing any
 * form actions.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class AdministratorUIComponent<P extends Action>
    extends UIComponentSupport<P>
{
    protected AdministratorUIComponent(final P parent) {
        super(parent);
    }

    @Override
    public Permission getViewPermission() {
        return Hudson.ADMINISTER;
    }

    protected void checkPermission() {
        checkPermission(Hudson.ADMINISTER);
    }
}
