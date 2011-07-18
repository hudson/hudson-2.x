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

package hudson.os;

import hudson.remoting.Callable;
import hudson.util.StreamTaskListener;

import java.io.FileOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class SUTester {
    public static void main(String[] args) throws Throwable {
        SU.execute(StreamTaskListener.fromStdout(),"kohsuke","bogus",new Callable<Object, Throwable>() {
            public Object call() throws Throwable {
                System.out.println("Touching /tmp/x");
                new FileOutputStream("/tmp/x").close();
                return null;
            }
        });
    }
}
