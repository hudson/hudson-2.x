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
 * DataStructure to hold the memory Usage data of a System
 *  
 */
public interface NativeSystemMemory {

    /**
     * Get the available System Memory
     * @return 
     */
    public long getAvailablePhysicalMemory();

    /**
     * Get the available Swap Space
     * @return 
     */
    public long getAvailableSwapSpace();

    /**
     * Get the available Total Physical memory
     * @return 
     */
    public long getTotalPhysicalMemory();

    /** 
     * Get the available Total Swap Space
     * @return 
     */
    public long getTotalSwapSpace(); 
}
