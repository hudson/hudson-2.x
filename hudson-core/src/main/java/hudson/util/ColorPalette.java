/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Inc., Kohsuke Kawaguchi, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.util;


import java.awt.Color;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * Color constants consistent with the Hudson color palette. 
 *
 * @author Kohsuke Kawaguchi
 */
public class ColorPalette {
    public static final Color RED = new Color(0xEF,0x29,0x29);
    public static final Color YELLOW = new Color(0xFC,0xE9,0x4F);
    public static final Color BLUE = new Color(0x72,0x9F,0xCF);
    public static final Color GREY = new Color(0xAB,0xAB,0xAB);
    
    /**
     * Color list usable for generating line charts.
     */
    public static List<Color> LINE_GRAPH = Collections.unmodifiableList(Arrays.asList(
        new Color(0xCC0000),
        new Color(0x3465a4),
        new Color(0x73d216),
        new Color(0xedd400)
    ));
}
