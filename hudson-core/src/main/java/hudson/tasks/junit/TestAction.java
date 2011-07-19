/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.tasks.junit;

import hudson.model.Action;

/**
 * 
 * Jelly (all optional):
 * <ul>
 * <li>index.jelly: included at the top of the test page</li>
 * <li>summary.jelly: included in a collapsed panel on the test parent page</li>
 * <li>badge.jelly: shown after the test link on the test parent page</li>
 * </ul>
 * 
 * @author tom
 * @since 1.320
 * @see TestDataPublisher
 */
public abstract class TestAction implements Action {

	/**
	 * Returns text with annotations.
	 */
	public String annotate(String text) {
		return text;
	}

}
