/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Scans the fingerprint database and remove old records
 * that are no longer relevant.
 *
 * <p>
 * A {@link Fingerprint} is removed when none of the builds that
 * it point to is available in the records.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public final class FingerprintCleanupThread extends AsyncPeriodicWork {

    private static FingerprintCleanupThread theInstance;

    public FingerprintCleanupThread() {
        super("Fingerprint cleanup");
        theInstance = this;
    }

    public long getRecurrencePeriod() {
        return DAY;
    }

    public static void invoke() {
        theInstance.run();
    }

    protected void execute(TaskListener listener) {
        int numFiles = 0;

        File root = new File(Hudson.getInstance().getRootDir(),"fingerprints");
        File[] files1 = root.listFiles(LENGTH2DIR_FILTER);
        if(files1!=null) {
            for (File file1 : files1) {
                File[] files2 = file1.listFiles(LENGTH2DIR_FILTER);
                for(File file2 : files2) {
                    File[] files3 = file2.listFiles(FINGERPRINTFILE_FILTER);
                    for(File file3 : files3) {
                        if(check(file3))
                            numFiles++;
                    }
                    deleteIfEmpty(file2);
                }
                deleteIfEmpty(file1);
            }
        }

        logger.log(Level.INFO, "Cleaned up "+numFiles+" records");
    }

    /**
     * Deletes a directory if it's empty.
     */
    private void deleteIfEmpty(File dir) {
        String[] r = dir.list();
        if(r==null)     return; // can happen in a rare occasion
        if(r.length==0)
            dir.delete();
    }

    /**
     * Examines the file and returns true if a file was deleted.
     */
    private boolean check(File fingerprintFile) {
        try {
            Fingerprint fp = Fingerprint.load(fingerprintFile);
            if(!fp.isAlive()) {
                fingerprintFile.delete();
                return true;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to process "+fingerprintFile, e);
        }
        return false;
    }

    private static final FileFilter LENGTH2DIR_FILTER = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory() && f.getName().length()==2;
        }
    };

    private static final FileFilter FINGERPRINTFILE_FILTER = new FileFilter() {
        private final Pattern PATTERN = Pattern.compile("[0-9a-f]{28}\\.xml");

        public boolean accept(File f) {
            return f.isFile() && PATTERN.matcher(f.getName()).matches();
        }
    };
}
