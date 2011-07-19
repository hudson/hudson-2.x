/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Kohsuke Kawaguchi  
 *
 *******************************************************************************/ 

/**
 * See {@link hudson.remoting.TrafficAnalyzer}. This entry point makes it easier to
 * invoke the tool.
 *
 * @author Kohsuke Kawaguchi
 */
public class TrafficAnalyzer {
    public static void main(String[] args) throws Exception {
        hudson.remoting.TrafficAnalyzer.main(args);
    }
}
