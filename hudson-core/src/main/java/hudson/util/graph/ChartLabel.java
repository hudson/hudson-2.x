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

import java.awt.Color;

/**
 * Information about Chart Label
 */
abstract public class ChartLabel implements Comparable<ChartLabel>{
    abstract public Color getColor(int row, int column);
    abstract public String getLink(int row, int column);
    abstract public String getToolTip(int row, int column);
}
