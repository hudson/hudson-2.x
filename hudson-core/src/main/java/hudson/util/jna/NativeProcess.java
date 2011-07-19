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

import java.util.Map;

/**
 * DataStructure that represents a Native Process
 */
public interface NativeProcess {

    /**
     * Get Process Id
     * @return 
     */
    public int getPid();
    /**
     * Get Parent Process ID
     * @return 
     */
    public int getPpid();
    /**
     * Kill this process and its children recursively
     */
    public void killRecursively();
    /**
     * Kill this process
     */
    public void kill();
    /**
     * Set the priority of this process
     * @param priority 
     */
    public void setPriority(int priority);
    /**
     * get the command line associated with this process
     * @return 
     */
    public String getCommandLine();
    /**
     * get the environment variables associated with this process
     * @return map Environment variable pairs
     */
    public Map<String, String> getEnvironmentVariables();

}
