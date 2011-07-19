/*******************************************************************************
 *
 * Copyright (c) 2009, Yahoo!, Inc.
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

package hudson.tasks.test;

import hudson.Extension;


/**
 * A class to exercise the TestResult extension mechanism.
 */
@Extension
public class TrivialTestResult extends SimpleCaseResult {


    /** A list only containing this one result. Useful for returning
     * from getXXXTests() methods.
     */
    

    /** A silly world that is all the data associated with this test result */
    private String sillyWord;

    public TrivialTestResult() {
        this("unwordy");
    }

    public TrivialTestResult(String sillyWord) {
        this.sillyWord = sillyWord;
    }

    public String getSillyWord() {
        return sillyWord;
    }

}
