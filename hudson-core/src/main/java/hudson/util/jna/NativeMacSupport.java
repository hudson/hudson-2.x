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
import java.util.List;

/**
 * Extension point for adding Native Mac Support to Hudson
 *
 * <p>
 * This object can have an optional <tt>config.jelly</tt> to configure the Native Access Support
 * <p>
 * A default constructor is needed to create NativeAccessSupport in the default configuration.
 *
 * @author Winston Prakash
 * @since 2.0.1
 * @see NativeAccessSupportDescriptor
 */
public abstract class NativeMacSupport extends AbstractDescribableImpl<NativeMacSupport> implements ExtensionPoint {

    /**
     * Returns all the registered {@link NativeAccessSupport} descriptors.
     */
    public static DescriptorExtensionList<NativeMacSupport, Descriptor<NativeMacSupport>> all() {
        return Hudson.getInstance().<NativeMacSupport, Descriptor<NativeMacSupport>>getDescriptorList(NativeMacSupport.class);
    }

    @Override
    public NativeMacSupportDescriptor getDescriptor() {
        return (NativeMacSupportDescriptor) super.getDescriptor();
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
     * Get the Native processes of a Mac System
     * @return
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public List<NativeProcess> getMacProcesses() throws NativeAccessException;
}
