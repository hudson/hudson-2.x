/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import java.util.Calendar;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see hudson.util.graph.Graph
 */
public class Graph extends hudson.util.graph.Graph {

    public Graph(long timestamp, int defaultW, int defaultH) {
        super(timestamp, defaultW, defaultH);
    }

    public Graph(Calendar timestamp, int defaultW, int defaultH) {
        super(timestamp, defaultW, defaultH);
    }
}
