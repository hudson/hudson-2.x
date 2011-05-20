/*
 * The MIT License
 *
 * Copyright 2011 Winston.Prakash@Oracle.com
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
