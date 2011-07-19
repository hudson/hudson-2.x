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

package org.jvnet.hudson.test;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.NullSCM;
import hudson.scm.SCM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * {@link SCM} useful for testing that extracts the given resource as a zip file.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExtractResourceWithChangesSCM extends NullSCM {
    private final URL firstZip;
    private final URL secondZip;
    private final String moduleRoot;
    
    public ExtractResourceWithChangesSCM(URL firstZip, URL secondZip) {
        if ((firstZip == null) || (secondZip == null))
            throw new IllegalArgumentException();
        this.firstZip = firstZip;
        this.secondZip = secondZip;
        this.moduleRoot = null;
    }

    public ExtractResourceWithChangesSCM(URL firstZip, URL secondZip, String moduleRoot) {
        if ((firstZip == null) || (secondZip == null))
            throw new IllegalArgumentException();
        this.firstZip = firstZip;
        this.secondZip = secondZip;
        this.moduleRoot = moduleRoot;
    }

    @Override
    public FilePath getModuleRoot(FilePath workspace) {
        if (moduleRoot!=null) {
            return workspace.child(moduleRoot);
        }
        return workspace;
    }
    
    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changeLogFile) throws IOException, InterruptedException {
        if (workspace.exists()) {
            listener.getLogger().println("Deleting existing workspace " + workspace.getRemote());
            workspace.deleteRecursive();
        }
        listener.getLogger().println("Staging first zip: " + firstZip);
        workspace.unzipFrom(firstZip.openStream());
        listener.getLogger().println("Staging second zip: " + secondZip);
        workspace.unzipFrom(secondZip.openStream());

        // Get list of files changed in secondZip.
        ZipInputStream zip = new ZipInputStream(secondZip.openStream());
        ZipEntry e;
        ExtractChangeLogParser.ExtractChangeLogEntry changeLog = new ExtractChangeLogParser.ExtractChangeLogEntry(secondZip.toString());

        try {
            while ((e = zip.getNextEntry()) != null) {
                if (!e.isDirectory())
                    changeLog.addFile(new ExtractChangeLogParser.FileInZip(e.getName()));
            }
        }
        finally {
            zip.close();
        }
        saveToChangeLog(changeLogFile, changeLog);

        return true;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new ExtractChangeLogParser();
    }

    private static String escapeForXml(String string) {
        return Util.xmlEscape(Util.fixNull(string));
    }

    public void saveToChangeLog(File changeLogFile, ExtractChangeLogParser.ExtractChangeLogEntry changeLog) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(changeLogFile);

        PrintStream stream = new PrintStream(outputStream, false, "UTF-8");

        stream.println("<?xml version='1.0' encoding='UTF-8'?>");
        stream.println("<extractChanges>");
        stream.println("<entry>");
        stream.println("<zipFile>" + escapeForXml(changeLog.getZipFile()) + "</zipFile>");
        stream.println("<file>");

        for (String fileName : changeLog.getAffectedPaths()) {
            stream.println("<fileName>" + escapeForXml(fileName) + "</fileName>");
        }

        stream.println("</file>");
        stream.println("</entry>");
        stream.println("</extractChanges>");

        stream.close();
    }

    /**
     * Don't write 'this', so that subtypes can be implemented as anonymous class.
     */
    private Object writeReplace() { return new Object(); }
}
