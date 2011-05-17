/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Winston Prakash
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
