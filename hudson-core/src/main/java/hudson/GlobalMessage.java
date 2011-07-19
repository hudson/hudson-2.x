/*******************************************************************************
 *
 * Copyright (c) 2011 Sonatype, Inc.
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

package hudson;

/**
 * Allows custom messages to be added to each page.
 *
 * Requires stapler view <tt>detail.jelly</tt>.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class GlobalMessage
        implements ExtensionPoint {

    public boolean isEnabled() {
        return true;
    }
}
