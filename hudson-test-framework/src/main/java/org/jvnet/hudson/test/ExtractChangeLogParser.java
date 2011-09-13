/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Nikita Levyankov
 *     
 *
 *******************************************************************************/ 

package org.jvnet.hudson.test;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import org.apache.commons.digester3.Digester;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Andrew Bayer
 */
public class ExtractChangeLogParser extends ChangeLogParser {
    @Override
    public ExtractChangeLogSet parse(AbstractBuild build, File changeLogFile) throws IOException, SAXException {
        if (changeLogFile.exists()) {
            FileInputStream fis = new FileInputStream(changeLogFile);
            ExtractChangeLogSet logSet = parse(build, fis);
            fis.close();
            return logSet;
        } else {
            return new ExtractChangeLogSet(build, new ArrayList<ExtractChangeLogEntry>());
        }
    }

    public ExtractChangeLogSet parse(AbstractBuild build, InputStream changeLogStream) throws IOException, SAXException {

        ArrayList<ExtractChangeLogEntry> changeLog = new ArrayList<ExtractChangeLogEntry>();

        Digester digester = new Digester();
        digester.setClassLoader(ExtractChangeLogSet.class.getClassLoader());
        digester.push(changeLog);
        digester.addObjectCreate("*/extractChanges/entry", ExtractChangeLogEntry.class);

        digester.addBeanPropertySetter("*/extractChanges/entry/zipFile");

        digester.addObjectCreate("*/extractChanges/entry/file",
                FileInZip.class);
        digester.addBeanPropertySetter("*/extractChanges/entry/file/fileName");
        digester.addSetNext("*/extractChanges/entry/file", "addFile");
        digester.addSetNext("*/extractChanges/entry", "add");

        digester.parse(changeLogStream);

        return new ExtractChangeLogSet(build, changeLog);
    }


    @ExportedBean(defaultVisibility = 999)
    public static class ExtractChangeLogEntry extends ChangeLogSet.Entry {
        private List<FileInZip> files = new ArrayList<FileInZip>();
        private String zipFile;

        public ExtractChangeLogEntry() {
        }

        public ExtractChangeLogEntry(String zipFile) {
            this.zipFile = zipFile;
        }

        public void setZipFile(String zipFile) {
            this.zipFile = zipFile;
        }

        @Exported
        public String getZipFile() {
            return zipFile;
        }

        @Override
        public void setParent(ChangeLogSet parent) {
            super.setParent(parent);
        }

        public Collection<String> getAffectedPaths() {
            Collection<String> paths = new ArrayList<String>(files.size());
            for (FileInZip file : files) {
                paths.add(file.getFileName());
            }
            return paths;
        }

        @Exported
        public User getAuthor() {
            return User.get("testuser");
        }

        @Exported
        public String getMsg() {
            return "Extracted from " + zipFile;
        }

        public void addFile(FileInZip fileName) {
            files.add(fileName);
        }

        public void addFiles(Collection<FileInZip> fileNames) {
            this.files.addAll(fileNames);
        }

        public String getUser() {
            return getAuthor().getDisplayName();
        }
    }

    @ExportedBean(defaultVisibility = 999)
    public static class FileInZip {
        private String fileName = "";

        public FileInZip() {
        }

        public FileInZip(String fileName) {
            this.fileName = fileName;
        }

        @Exported
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

}
