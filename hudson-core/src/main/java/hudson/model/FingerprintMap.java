/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
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

import hudson.Util;
import hudson.diagnosis.OldDataMonitor;
import hudson.util.KeyedDataStorage;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache of {@link Fingerprint}s.
 *
 * <p>
 * This implementation makes sure that no two {@link Fingerprint} objects
 * lie around for the same hash code, and that unused {@link Fingerprint}
 * will be adequately GC-ed to prevent memory leak.
 *
 * @author Kohsuke Kawaguchi
 * @see Hudson#getFingerprintMap() 
 */
public final class FingerprintMap extends KeyedDataStorage<Fingerprint,FingerprintParams> {

    /**
     * @deprecated since 2007-03-26.
     *      Some old version of Hudson incorrectly serialized this information to the disk.
     *      So we need this field to be here for such configuration to be read correctly.
     *      This field is otherwise no longer in use.
     */
    private transient ConcurrentHashMap<String,Object> core = new ConcurrentHashMap<String,Object>();

    /**
     * Returns true if there's some data in the fingerprint database.
     */
    public boolean isReady() {
        return new File(Hudson.getInstance().getRootDir(),"fingerprints").exists();
    }

    /**
     * @param build
     *      set to non-null if {@link Fingerprint} to be created (if so)
     *      will have this build as the owner. Otherwise null, to indicate
     *      an owner-less build.
     */
    public Fingerprint getOrCreate(AbstractBuild build, String fileName, byte[] md5sum) throws IOException {
        return getOrCreate(build,fileName, Util.toHexString(md5sum));
    }

    public Fingerprint getOrCreate(AbstractBuild build, String fileName, String md5sum) throws IOException {
        return super.getOrCreate(md5sum, new FingerprintParams(build,fileName));
    }

    public Fingerprint getOrCreate(Run build, String fileName, String md5sum) throws IOException {
        return super.getOrCreate(md5sum, new FingerprintParams(build,fileName));
    }

    @Override
    protected Fingerprint get(String md5sum, boolean createIfNotExist, FingerprintParams createParams) throws IOException {
        // sanity check
        if(md5sum.length()!=32)
            return null;    // illegal input
        md5sum = md5sum.toLowerCase(Locale.ENGLISH);

        return super.get(md5sum,createIfNotExist,createParams);
    }

    private byte[] toByteArray(String md5sum) {
        byte[] data = new byte[16];
        for( int i=0; i<md5sum.length(); i+=2 )
            data[i/2] = (byte)Integer.parseInt(md5sum.substring(i,i+2),16);
        return data;
    }

    protected Fingerprint create(String md5sum, FingerprintParams createParams) throws IOException {
        return new Fingerprint(createParams.build, createParams.fileName, toByteArray(md5sum));
    }

    protected Fingerprint load(String key) throws IOException {
        return Fingerprint.load(toByteArray(key));
    }

    private Object readResolve() {
        if (core != null) OldDataMonitor.report(Hudson.getInstance(), "1.91");
        return this;
    }
}

class FingerprintParams {
    /**
     * Null if the build isn't claiming to be the owner.
     */
    final Run build;
    final String fileName;

    public FingerprintParams(Run build, String fileName) {
        this.build = build;
        this.fileName = fileName;

        assert fileName!=null;
    }
}
