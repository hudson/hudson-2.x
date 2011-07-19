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
 * Constants Describing Native functions
 */
public enum NativeFunction {

    CHMOD,
    FILE_WRITABLE,
    SYMLINK,
    RESOLVE_LINK,
    DOTNET,
    SYSTEM_MEMORY,
    EUID,
    EGID,
    WINDOWS_PROCESS,
    WINDOWS_EXEC,
    WINDOWS_FILE_MOVE,
    MAC_PROCESS,
    JAVA_RESTART,
    UNIX_USER,
    ZFS,
    CHOWN,
    MODE,
    UNIX_GROUP,
    PAM,
    ERROR
}
