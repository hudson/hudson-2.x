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

package hudson.tasks;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.BuildPtr;
import hudson.model.FingerprintMap;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.RunAction;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import hudson.util.IOException2;
import hudson.util.PackedMap;
import net.sf.json.JSONObject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Records fingerprints of the specified files.
 *
 * @author Kohsuke Kawaguchi
 */
public class Fingerprinter extends Recorder implements Serializable {

    /**
     * Comma-separated list of files/directories to be fingerprinted.
     */
    private final String targets;

    /**
     * Also record all the finger prints of the build artifacts.
     */
    private final boolean recordBuildArtifacts;

    @DataBoundConstructor
    public Fingerprinter(String targets, boolean recordBuildArtifacts) {
        this.targets = targets;
        this.recordBuildArtifacts = recordBuildArtifacts;
    }

    public String getTargets() {
        return targets;
    }

    public boolean getRecordBuildArtifacts() {
        return recordBuildArtifacts;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        try {
            listener.getLogger().println(Messages.Fingerprinter_Recording());

            Map<String,String> record = new HashMap<String,String>();


            if(targets.length()!=0)
                record(build, listener, record, targets);

            if(recordBuildArtifacts) {
                ArtifactArchiver aa = build.getProject().getPublishersList().get(ArtifactArchiver.class);
                if(aa==null) {
                    // configuration error
                    listener.error(Messages.Fingerprinter_NoArchiving());
                    build.setResult(Result.FAILURE);
                    return true;
                }
                record(build, listener, record, aa.getArtifacts() );
            }

            FingerprintAction.add(build, record);

        } catch (IOException e) {
            e.printStackTrace(listener.error(Messages.Fingerprinter_Failed()));
            build.setResult(Result.FAILURE);
        }

        // failing to record fingerprints is an error but not fatal
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private void record(AbstractBuild<?,?> build, BuildListener listener, Map<String,String> record, final String targets) throws IOException, InterruptedException {
        final class Record implements Serializable {
            final boolean produced;
            final String relativePath;
            final String fileName;
            final String md5sum;

            public Record(boolean produced, String relativePath, String fileName, String md5sum) {
                this.produced = produced;
                this.relativePath = relativePath;
                this.fileName = fileName;
                this.md5sum = md5sum;
            }

            Fingerprint addRecord(AbstractBuild build) throws IOException {
                FingerprintMap map = Hudson.getInstance().getFingerprintMap();
                return map.getOrCreate(produced?build:null, fileName, md5sum);
            }

            private static final long serialVersionUID = 1L;
        }

        final long buildTimestamp = build.getTimeInMillis();

        FilePath ws = build.getWorkspace();
        if(ws==null) {
            listener.error(Messages.Fingerprinter_NoWorkspace());
            build.setResult(Result.FAILURE);
            return;
        }

        List<Record> records = ws.act(new FileCallable<List<Record>>() {
            public List<Record> invoke(File baseDir, VirtualChannel channel) throws IOException {
                List<Record> results = new ArrayList<Record>();

                FileSet src = Util.createFileSet(baseDir,targets);

                DirectoryScanner ds = src.getDirectoryScanner();
                for( String f : ds.getIncludedFiles() ) {
                    File file = new File(baseDir,f);

                    // consider the file to be produced by this build only if the timestamp
                    // is newer than when the build has started.
                    // 2000ms is an error margin since since VFAT only retains timestamp at 2sec precision
                    boolean produced = buildTimestamp <= file.lastModified()+2000;

                    try {
                        results.add(new Record(produced,f,file.getName(),new FilePath(file).digest()));
                    } catch (IOException e) {
                        throw new IOException2(Messages.Fingerprinter_DigestFailed(file),e);
                    } catch (InterruptedException e) {
                        throw new IOException2(Messages.Fingerprinter_Aborted(),e);
                    }
                }

                return results;
            }
        });

        for (Record r : records) {
            Fingerprint fp = r.addRecord(build);
            if(fp==null) {
                listener.error(Messages.Fingerprinter_FailedFor(r.relativePath));
                continue;
            }
            fp.add(build);
            record.put(r.relativePath,fp.getHashString());
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return Messages.Fingerprinter_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/project-config/fingerprint.html";
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public FormValidation doCheck(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(),value);
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) {
            return req.bindJSON(Fingerprinter.class, formData);
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    /**
     * Action for displaying fingerprints.
     * 
     * To ensure there is only one per build use {@link FingerprintAction#add(AbstractBuild, Map)}.
     * This allows for additional fingerprint contributions outside of the {@link Fingerprinter}.
     */
    public static final class FingerprintAction implements RunAction {
        private final AbstractBuild build;

        /**
         * From file name to the digest.
         */
        private /*almost final*/ Map<String,String> record;

        private transient WeakReference<Map<String,Fingerprint>> ref;

        public FingerprintAction(AbstractBuild build, Map<String, String> record) {
            this.build = checkNotNull(build);
            this.record = PackedMap.of(checkNotNull(record));
        }

        /**
         * Add fingerprint records to this Action.  Assumes the records came from the same build that initially
         * created the {@link FingerprintAction}.
         */
        public void add(Map<String,String> moreRecords) {
            Map<String,String> r = new HashMap<String, String>(record);
            r.putAll(moreRecords);
            record = PackedMap.of(checkNotNull(r));
            ref = null;
        }

        /**
         * Adds the record to a {@link FingerprintAction} corresponding to the build.
         *
         * Safely consolidates multiple sources of records (e.g. from different post build actions) into a single
         * {@link FingerprintAction}.
         *
         * @param build to add the FingerprintAction and records to
         * @param record to add
         *
         * @since 2.1.0
         */
        public static void add(final AbstractBuild build, final Map<String, String> record) {
            checkNotNull(build);
            checkNotNull(record);

            FingerprintAction action = build.getAction(FingerprintAction.class);
            if(action != null) {
                action.add(record);
            } else {
                build.addAction(new FingerprintAction(build,record));
            }
        }

        public String getIconFileName() {
            return "fingerprint.gif";
        }

        public String getDisplayName() {
            return Messages.Fingerprinter_Action_DisplayName();
        }

        public String getUrlName() {
            return "fingerprints";
        }

        public AbstractBuild getBuild() {
            return build;
        }

        /**
         * Obtains the raw data.
         */
        public Map<String,String> getRecords() {
            return record;
        }

        public void onLoad() {
            Run pb = build.getPreviousBuild();
            if (pb!=null) {
                FingerprintAction a = pb.getAction(FingerprintAction.class);
                if (a!=null)
                    compact(a);
            }
        }

        public void onAttached(Run r) {
        }

        public void onBuildComplete() {
            onLoad();   // make compact
        }

        /**
         * Reuse string instances from another {@link FingerprintAction} to reduce memory footprint.
         */
        protected void compact(FingerprintAction a) {
            Map<String,String> intern = new HashMap<String, String>(); // string intern map
            for (Entry<String, String> e : a.record.entrySet()) {
                intern.put(e.getKey(),e.getKey());
                intern.put(e.getValue(),e.getValue());
            }

            Map<String,String> b = new HashMap<String, String>();
            for (Entry<String,String> e : record.entrySet()) {
                String k = intern.get(e.getKey());
                if (k==null)    k = e.getKey();

                String v = intern.get(e.getValue());
                if (v==null)    v = e.getValue();

                b.put(k,v);
            }

            record = PackedMap.of(b);
        }

        /**
         * Map from file names of the fingerprinted file to its fingerprint record.
         */
        public synchronized Map<String,Fingerprint> getFingerprints() {
            if(ref!=null) {
                Map<String,Fingerprint> m = ref.get();
                if(m!=null)
                    return m;
            }

            Hudson h = Hudson.getInstance();

            Map<String,Fingerprint> m = new TreeMap<String,Fingerprint>();
            for (Entry<String, String> r : record.entrySet()) {
                try {
                    Fingerprint fp = h._getFingerprint(r.getValue());
                    if(fp!=null)
                        m.put(r.getKey(), fp);
                } catch (IOException e) {
                    logger.log(Level.WARNING,e.getMessage(),e);
                }
            }

            m = ImmutableMap.copyOf(m);
            ref = new WeakReference<Map<String,Fingerprint>>(m);
            return m;
        }

        /**
         * Gets the dependency to other builds in a map.
         * Returns build numbers instead of {@link Build}, since log records may be gone.
         */
        public Map<AbstractProject,Integer> getDependencies() {
            Map<AbstractProject,Integer> r = new HashMap<AbstractProject,Integer>();

            for (Fingerprint fp : getFingerprints().values()) {
                BuildPtr bp = fp.getOriginal();
                if(bp==null)    continue;       // outside Hudson
                if(bp.is(build))    continue;   // we are the owner
                AbstractProject job = bp.getJob();
                if (job==null)  continue;   // no longer exists
                if (job.getParent()==build.getParent())
                    continue;   // we are the parent of the build owner, that is almost like we are the owner 

                Integer existing = r.get(job);
                if(existing!=null && existing>bp.getNumber())
                    continue;   // the record in the map is already up to date
                r.put(job,bp.getNumber());
            }
            
            return r;
        }
    }

    private static final Logger logger = Logger.getLogger(Fingerprinter.class.getName());

    private static final long serialVersionUID = 1L;
}
