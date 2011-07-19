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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package org.jvnet.hudson.test.rhino;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.debug.DebugFrame;
import net.sourceforge.htmlunit.corejs.javascript.debug.DebuggableScript;
import net.sourceforge.htmlunit.corejs.javascript.debug.Debugger;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * Monitors the execution of the JavaScript inside HtmlUnit, and provides debug information
 * such as call stacks, variables, arguments, etc.
 *
 * <h2>Usage</h2>
 * <p>
 * When you set a break point in Java code that are directly/indirectly invoked through JavaScript,
 * use {@link #toString()} to see the JavaScript stack trace (and variables at each stack frame.)
 * This helps you see where the problem is.
 *
 * <p>
 * TODO: add programmatic break point API, selective method invocation tracing, and
 * allow arbitrary script evaluation in arbitrary stack frame.
 *
 * @author Kohsuke Kawaguchi
 * @see HudsonTestCase#jsDebugger
 */
public class JavaScriptDebugger implements Debugger {
    /**
     * Call stack as a list. The list grows at the end, so the first element in the list
     * is the oldest stack frame.
     */
    public final List<CallStackFrame> callStack = new ArrayList<CallStackFrame>();

    public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
    }

    public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
        return new CallStackFrame(this,fnOrScript);
    }

    /**
     * Formats the current call stack into a human readable string.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for( int i=callStack.size()-1; i>=0; i-- )
            buf.append(callStack.get(i)).append('\n');
        return buf.toString();
    }
}
