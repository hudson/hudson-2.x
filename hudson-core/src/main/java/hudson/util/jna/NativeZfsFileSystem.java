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

import java.io.File;

/**
 * Data Structure and operation defining a ZFS file Systems
 */
public interface NativeZfsFileSystem{
    
    public static final int MS_RDONLY = 1;
    public static final int MS_FSS = 2;
    public static final int MS_DATA = 4;
    public static final int MS_NOSUID = 16;
    public static final int MS_REMOUNT = 32;
    public static final int MS_NOTRUNC = 64;
    public static final int MS_OVERLAY = 128;
    public static final int MS_OPTIONSTR = 256;
    public static final int MS_GLOBAL = 512;
    public static final int MS_FORCE = 1024;
    public static final int MS_NOMNTTAB = 2048;
    
    public String getName();

    public void setMountPoint(File dir);

    public void mount();

    public void unmount();
    
    public void unmount(int flag);

    public void setProperty(String string, String string0);

    public void destory();

    public void allow(String userName);

    public void share();

    public void destory(boolean recursive);
}
