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

package hudson.util.jna;


/**
 * Exception to be thrown when a native function is not supported
 * or when the function failed to execute
 * 
 */
public class NativeAccessException extends Exception {
    
    public static int PERMISSION = 1;
    
    private int code;
    

    public NativeAccessException(String errorMsg) {
        super(errorMsg);
    }
    
    public NativeAccessException(String errorMsg, int code) {
        super(errorMsg);
        this.code = code;
    }

    public NativeAccessException(Throwable exc) {
        super(exc);
    }
    
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
