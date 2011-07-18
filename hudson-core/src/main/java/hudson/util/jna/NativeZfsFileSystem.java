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
