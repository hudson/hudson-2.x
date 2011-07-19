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
 *    Kohsuke Kawaguchi, Winston Prakash
 *     
 *
 *******************************************************************************/ 

package hudson.util.graph;

import hudson.model.AbstractBuild;
import java.awt.Font;


/**
 * Chart generation utility code around JFreeChart.
 *
 * @see StackedAreaRenderer2
 * @see DataSetBuilder
 * @see ShiftedCategoryAxis
 * @author Kohsuke Kawaguchi
 */
public class ChartUtil {
    
    /**
     * @deprecated
     *      Use {@code awtProblemCause!=null} instead. As of 1.267.
     */
    public static boolean awtProblem = false;

    /**
     * See issue 93. Detect an error in X11 and handle it gracefully.
     */
    public static Throwable awtProblemCause = null;
    
    static {
        try {
            new Font("SansSerif",Font.BOLD,18).toString();
        } catch (Throwable t) {
            awtProblemCause = t;
            awtProblem = true;
        }
    }
    
    
    /**
     * Can be used as a graph label. Only displays numbers.
     */
    public static abstract class NumberOnlyBuildLabel extends ChartLabel {
        public final AbstractBuild build;

        public NumberOnlyBuildLabel(AbstractBuild build) {
            this.build = build;
        }

        public int compareTo(NumberOnlyBuildLabel that) {
            return this.build.number-that.build.number;
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof NumberOnlyBuildLabel))    return false;
            NumberOnlyBuildLabel that = (NumberOnlyBuildLabel) o;
            return build==that.build;
        }

        @Override
        public int hashCode() {
            return build.hashCode();
        }

        @Override
        public String toString() {
            return build.getDisplayName();
        }
    }
}
