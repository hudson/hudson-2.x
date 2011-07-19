/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.os.solaris;

import hudson.FileSystemProvisioner;
import hudson.FilePath;
import hudson.WorkspaceSnapshot;
import hudson.FileSystemProvisionerDescriptor;
import hudson.Extension;
import hudson.remoting.VirtualChannel;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.util.jna.NativeAccessException;
import hudson.util.jna.NativeUtils;
import hudson.util.jna.NativeZfsFileSystem;

import java.io.IOException;
import java.io.File;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link FileSystemProvisioner} for ZFS.
 *
 * @author Kohsuke Kawaguchi
 */
public class ZFSProvisioner extends FileSystemProvisioner implements Serializable {
    
    private NativeUtils nativeUtils = NativeUtils.getInstance();
    private final Node node;
    private final String rootDataset;

    public ZFSProvisioner(Node node) throws IOException, InterruptedException {
        this.node = node;
        rootDataset = node.getRootPath().act(new FileCallable<String>() {
            public String invoke(File f, VirtualChannel channel) throws IOException {
                try {
                    NativeZfsFileSystem fs = nativeUtils.getZfsByMountPoint(f);
                    if(fs != null)    return fs.getName();
                } catch (NativeAccessException ex) {
                    Logger.getLogger(ZFSProvisioner.class.getName()).log(Level.SEVERE, null, ex);
                }
                 
                // TODO: for now, only support slaves that are already on ZFS.
                throw new IOException("Not on ZFS");
            }
        });
    }

    public void prepareWorkspace(AbstractBuild<?,?> build, FilePath ws, final TaskListener listener) throws IOException, InterruptedException {
        final String name = build.getProject().getFullName();
        
        ws.act(new FileCallable<Void>() {

            public Void invoke(File f, VirtualChannel channel) throws IOException {
                try {
                    NativeZfsFileSystem fs = nativeUtils.getZfsByMountPoint(f);
                    if (fs != null) {
                        return null;    // already on ZFS
                    }
                    // nope. create a file system
                    String fullName = rootDataset + '/' + name;
                    listener.getLogger().println("Creating a ZFS file system " + fullName + " at " + f);
                    fs = nativeUtils.createZfs(fullName);
                    fs.setMountPoint(f);
                    fs.mount();
                } catch (NativeAccessException ex) {
                    Logger.getLogger(ZFSProvisioner.class.getName()).log(Level.SEVERE, null, ex);
                }

                return null;
            }
        });
    }

    public void discardWorkspace(AbstractProject<?, ?> project, FilePath ws) throws IOException, InterruptedException {
        ws.act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                try {
                    NativeZfsFileSystem fs = nativeUtils.getZfsByMountPoint(f);
                    if(fs != null){
                       fs.destory(true);
                    }
                } catch (NativeAccessException ex) {
                    Logger.getLogger(ZFSProvisioner.class.getName()).log(Level.SEVERE, null, ex);
                }
                 
                return null;
            }
        });
    }

    /**
     * @deprecated as of 1.350
     */
    public WorkspaceSnapshot snapshot(AbstractBuild<?, ?> build, FilePath ws, TaskListener listener) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    public WorkspaceSnapshot snapshot(AbstractBuild<?, ?> build, FilePath ws, String glob, TaskListener listener) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Extension
    public static final class DescriptorImpl extends FileSystemProvisionerDescriptor {
        public boolean discard(FilePath ws, TaskListener listener) throws IOException, InterruptedException {
            // TODO
            return false;
        }

        public String getDisplayName() {
            return "ZFS";
        }
    }

    private static final long serialVersionUID = 1L;
}
