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

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import java.io.File;
import java.util.List;

/**
 * Extension point for adding Native Access Support to ZFS FileSystem
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
public abstract class NativeZfsSupport extends AbstractDescribableImpl<NativeZfsSupport> implements ExtensionPoint {

    /**
     * Returns all the registered {@link NativeAccessSupport} descriptors.
     */
    public static DescriptorExtensionList<NativeZfsSupport, Descriptor<NativeZfsSupport>> all() {
        return Hudson.getInstance().<NativeZfsSupport, Descriptor<NativeZfsSupport>>getDescriptorList(NativeZfsSupport.class);
    }

    @Override
    public NativeZfsSupportDescriptor getDescriptor() {
        return (NativeZfsSupportDescriptor) super.getDescriptor();
    }

    /**
     * Check if this Extension has Support for specific native Operation
     * @param nativeFunc Native Operation
     * @return true if supported
     */
    abstract public boolean hasSupportFor(NativeFunction nativeFunc);

    /**
     * Fetch the list of mounted ZFS roots
     * @return
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public List<NativeZfsFileSystem> getZfsRoots() throws NativeAccessException;

    /**
     * Find the ZFS File System by its mount point
     * @return
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public NativeZfsFileSystem getZfsByMountPoint(File mountPoint) throws NativeAccessException;

    /** 
     * Create ZFS File System corresponding to the mount name
     * @param mountPoint
     * @return ZFS File System if created successfully
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public NativeZfsFileSystem createZfs(String mountName) throws NativeAccessException;

     /** 
     * Open the target ZFS File System 
     * @param mountPoint
     * @return ZFS File System if opened successfully
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public NativeZfsFileSystem openZfs(String target) throws NativeAccessException;

    /**
     * Check if the named ZFS exists
     * @param zfsName
     * @return
     * @throws hudson.util.jna.Native.NativeExecutionException 
     */
    abstract public  boolean zfsExists(String zfsName)  throws NativeAccessException;

    /**
     * Get the error associated with the last Operation
     * @return String error message
     */
    abstract public String getLastError();
}
