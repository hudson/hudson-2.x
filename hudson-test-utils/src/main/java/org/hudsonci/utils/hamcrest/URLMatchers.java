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

package org.hudsonci.utils.hamcrest;

import java.net.URL;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class URLMatchers {

    @Factory
    public static Matcher<URL> respondsWithStatus(int statusCode){
        return new URLRespondsWithStatusMatcher(statusCode);
    }

    @Factory
    public static Matcher<URL> respondsWithStatusWithin(int statusCode, int timeoutMillis){
        return new URLRespondsWithStatusMatcher(statusCode, timeoutMillis);
    }
}
