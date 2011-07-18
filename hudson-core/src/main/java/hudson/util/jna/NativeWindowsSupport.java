/*
 * The MIT License
 * 
 * Copyright (c) 2011, Oracle Corporation, Winston Prakash 
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

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import java.io.File;
import java.util.List;

/**
 * Extension point for adding Native Windows Support to Hudson
 *
 * <p>
 * This object can have an optional <tt>config.jelly</tt> to configure the Native Access Support
 * <p>
 * A default constructor is needed to create NativeAccessSupport in the default configuration.
 *
 * @author Winston Prakash
 * @since 2.0.1
 * @see NativeWindowsSupportDescriptor
 */
public abstract class NativeWindowsSupport extends AbstractDescribableImpl<NativeWindowsSupport> implements ExtensionPoint {

    /**
     * Returns all the registered {@link NativeAccessSupport} descriptors.
     */
    public static DescriptorExtensionList<NativeWindowsSupport, Descriptor<NativeWindowsSupport>> all() {
        return Hudson.getInstance().<NativeWindowsSupport, Descriptor<NativeWindowsSupport>>getDescriptorList(NativeWindowsSupport.class);
    }

    @Override
    public NativeWindowsSupportDescriptor getDescriptor() {
        return (NativeWindowsSupportDescriptor) super.getDescriptor();
    }

    /**
     * Check if this Extension has Support for specific native Operation
     * @param nativeFunc Native Operation
     * @return true if supported
     */
    abstract public boolean hasSupportFor(NativeFunction nativeFunc);

    /**
     * Get the error associated with the last Operation
     * @return String error message
     */
    abstract public String getLastError();

    /**
     * Check if .NET is installed on a the Windows machine
     * @return true if .NET is installed.
     * @throws hudson.util.jna.Native.ExecutionError 
     */
    abstract public boolean isDotNetInstalled(int major, int minor) throws NativeAccessException;

    /**
     * Get all the native processes on a Windows System
     * @return List of Native Window Processes
     * @throws hudson.util.jna.Native.ExecutionError 
     */
    abstract public List<NativeProcess> getWindowsProcesses() throws NativeAccessException;

    /**
     * Find the Native Process Id of the given java.lang.process
     * @param process (java.lang.process)
     * @return pid, the Native Process ID
     */
    abstract public int getWindowsProcessId(Process process) throws NativeAccessException;

    /**
     * Run the Windows program natively in an elevated privilege
     * @param winExe, windows executable to run
     * @param args, arguments to pass
     * @param logFile, File where the logs of the process should go
     * @param pwd, Path of the working directory
     * @return int, process exit code
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public int windowsExec(File winExe, String args, String logFile, File pwd) throws NativeAccessException;

    /**
     * Move a Windows File using native win32 library
     * @param fromFile
     * @param toFile
     * @return
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public void windowsMoveFile(File fromFile, File toFile) throws NativeAccessException;
}
