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

package org.eclipse.hudson.gwt.common.waitdialog;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.gwt.common.waitdialog.internal.PleaseWaitDialog;

/**
 * Widget that can inform the user of wait events.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(PleaseWaitDialog.class)
public interface WaitPresenter
{
    void startWaiting();
    
    void stopWaiting();
}
