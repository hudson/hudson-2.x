/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi, Yahoo! Inc., Seiji Sogabe
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
package hudson.model;

import hudson.BulkChange;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.XmlFile;
import static hudson.init.InitMilestone.PLUGINS_STARTED;
import hudson.init.Initializer;
import hudson.lifecycle.Lifecycle;
import hudson.model.UpdateSite.Data;
import hudson.model.UpdateSite.Plugin;
import hudson.model.listeners.SaveableListener;
import hudson.security.ACL;
import hudson.util.DaemonThreadFactory;
import hudson.util.IOException2;
import hudson.util.PersistedList;
import hudson.util.XStream2;
import org.acegisecurity.Authentication;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.kohsuke.stapler.StaplerResponse;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acegisecurity.context.SecurityContextHolder;


/**
 * Controls update center capability.
 *
 * <p>
 * The main job of this class is to keep track of the latest update center metadata file, and perform installations.
 * Much of the UI about choosing plugins to install is done in {@link PluginManager}.
 * <p>
 * The update center can be configured to contact alternate servers for updates
 * and plugins, and to use alternate strategies for downloading, installing
 * and updating components. See the Javadocs for {@link UpdateCenterConfiguration}
 * for more information.
 * 
 * @author Kohsuke Kawaguchi
 * @since 1.220
 */
public class UpdateCenter extends AbstractModelObject implements Saveable {
    /**
     * {@link ExecutorService} that performs installation.
     */
    private final ExecutorService installerService = Executors.newSingleThreadExecutor(
        new DaemonThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Update center installer thread");
                return t;
            }
        }));

    /**
     * List of created {@link UpdateCenterJob}s. Access needs to be synchronized.
     */
    private final Vector<UpdateCenterJob> jobs = new Vector<UpdateCenterJob>();

    /**
     * {@link UpdateSite}s from which we've already installed a plugin at least once.
     * This is used to skip network tests.
     */
    private final Set<UpdateSite> sourcesUsed = new HashSet<UpdateSite>();

    /**
     * List of {@link UpdateSite}s to be used.
     */
    private final PersistedList<UpdateSite> sites = new PersistedList<UpdateSite>(this);

    /**
     * Update center configuration data
     */
    private UpdateCenterConfiguration config;

    public UpdateCenter() {
        configure(new UpdateCenterConfiguration());
    }

    /**
     * Configures update center to get plugins/updates from alternate servers,
     * and optionally using alternate strategies for downloading, installing
     * and upgrading.
     *
     * @param config Configuration data
     * @see UpdateCenterConfiguration
     */
    public void configure(UpdateCenterConfiguration config) {
        if (config!=null) {
            this.config = config;
        }
    }

    /**
     * Returns the list of {@link UpdateCenterJob} representing scheduled installation attempts.
     *
     * @return
     *      can be empty but never null. Oldest entries first.
     */
    public List<UpdateCenterJob> getJobs() {
        synchronized (jobs) {
            return new ArrayList<UpdateCenterJob>(jobs);
        }
    }

    /**
     * Returns latest install/upgrade job for the given plugin.
     * @return InstallationJob or null if not found
     */
    public InstallationJob getJob(Plugin plugin) {
        List<UpdateCenterJob> jobList = getJobs();
        Collections.reverse(jobList);
        for (UpdateCenterJob job : jobList)
            if (job instanceof InstallationJob) {
                InstallationJob ij = (InstallationJob)job;
                if (ij.plugin.name.equals(plugin.name) && ij.plugin.sourceId.equals(plugin.sourceId))
                    return ij;
            }
        return null;
    }

    /**
     * Returns latest Hudson upgrade job.
     * @return HudsonUpgradeJob or null if not found
     */
    public HudsonUpgradeJob getHudsonJob() {
        List<UpdateCenterJob> jobList = getJobs();
        Collections.reverse(jobList);
        for (UpdateCenterJob job : jobList)
            if (job instanceof HudsonUpgradeJob)
                return (HudsonUpgradeJob)job;
        return null;
    }

    /**
     * Returns the list of {@link UpdateSite}s to be used.
     * This is a live list, whose change will be persisted automatically.
     *
     * @return
     *      can be empty but never null.
     */
    public PersistedList<UpdateSite> getSites() {
        return sites;
    }

    public UpdateSite getSite(String id) {
        for (UpdateSite site : sites)
            if (site.getId().equals(id))
                return site;
        return null;
    }

    /**
     * Gets the string representing how long ago the data was obtained.
     * Will be the newest of all {@link UpdateSite}s.
     */
    public String getLastUpdatedString() {
        long newestTs = -1;
        for (UpdateSite s : sites) {
            if (s.getDataTimestamp()>newestTs) {
                newestTs = s.getDataTimestamp();
            }
        }
        if(newestTs<0)     return "N/A";
        return Util.getPastTimeString(System.currentTimeMillis()-newestTs);
    }

    /**
     * Gets {@link UpdateSite} by its ID.
     * Used to bind them to URL.
     */
    public UpdateSite getById(String id) {
        for (UpdateSite s : sites) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets the {@link UpdateSite} from which we receive updates for <tt>hudson.war</tt>.
     *
     * @return
     *      null if no such update center is provided.
     */
    public UpdateSite getCoreSource() {
        for (UpdateSite s : sites) {
            Data data = s.getData();
            if (data!=null && data.core!=null)
                return s;
        }
        return null;
    }

    /**
     * Gets the default base URL.
     *
     * @deprecated
     *      TODO: revisit tool update mechanism, as that should be de-centralized, too. In the mean time,
     *      please try not to use this method, and instead ping us to get this part completed.
     */
    public String getDefaultBaseUrl() {
        return config.getUpdateCenterUrl();
    }

    /**
     * Gets the plugin with the given name from the first {@link UpdateSite} to contain it.
     */
    public Plugin getPlugin(String artifactId) {
        for (UpdateSite s : sites) {
            Plugin p = s.getPlugin(artifactId);
            if (p!=null) return p;
        }
        return null;
    }

    /**
     * Schedules a Hudson upgrade.
     */
    public void doUpgrade(StaplerResponse rsp) throws IOException, ServletException {
        requirePOST();
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        HudsonUpgradeJob job = new HudsonUpgradeJob(getCoreSource(), Hudson.getAuthentication());
        if(!Lifecycle.get().canRewriteHudsonWar()) {
            sendError("Hudson upgrade not supported in this running mode");
            return;
        }

        LOGGER.info("Scheduling the core upgrade");
        addJob(job);
        rsp.sendRedirect2(".");
    }

    /**
     * Returns true if backup of hudson.war exists on the hard drive
     */
    public boolean isDowngradable() {
        return new File(Lifecycle.get().getHudsonWar() + ".bak").exists();
    }

    /**
     * Performs hudson downgrade.
     */
    public void doDowngrade(StaplerResponse rsp) throws IOException, ServletException {
        requirePOST();
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        if(!isDowngradable()) {
            sendError("Hudson downgrade is not possible, probably backup does not exist");
            return;
        }

        HudsonDowngradeJob job = new HudsonDowngradeJob(getCoreSource(), Hudson.getAuthentication());
        LOGGER.info("Scheduling the core downgrade");
        addJob(job);
        rsp.sendRedirect2(".");
    }

    /**
     * Returns String with version of backup .war file,
     * if the file does not exists returns null
     */
    public String getBackupVersion()
    {
        try {
            JarFile backupWar = new JarFile(new File(Lifecycle.get().getHudsonWar().getParentFile(), "hudson.war.bak"));
            return backupWar.getManifest().getMainAttributes().getValue("Hudson-Version");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read backup version ", e);
            return null;}

    }

    /*package*/ synchronized Future<UpdateCenterJob> addJob(UpdateCenterJob job) {
        // the first job is always the connectivity check
        if (sourcesUsed.add(job.site))
            new ConnectionCheckJob(job.site).submit();
        return job.submit();
    }

    public String getDisplayName() {
        return "Update center";
    }

    public String getSearchUrl() {
        return "updateCenter";
    }

    /**
     * Saves the configuration info to the disk.
     */
    public synchronized void save() {
        if(BulkChange.contains(this))   return;
        try {
            getConfigFile().write(sites);
            SaveableListener.fireOnChange(this, getConfigFile());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save "+getConfigFile(),e);
        }
    }

    /**
     * Loads the data from the disk into this object.
     */
    public synchronized void load() throws IOException {
        UpdateSite defaultSite = new UpdateSite("default", config.getUpdateCenterUrl() + "update-center.json");
        XmlFile file = getConfigFile();
        if(file.exists()) {
            try {
                sites.replaceBy(((PersistedList)file.unmarshal(sites)).toList());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load "+file, e);
            }
            for (UpdateSite site : sites) {
                // replace the legacy site with the new site
                if (site.isLegacyDefault()) {
                    sites.remove(site);
                    sites.add(defaultSite);
                    break;
                }
            }
        } else {
            if (sites.isEmpty()) {
                // If there aren't already any UpdateSources, add the default one.
                // to maintain compatibility with existing UpdateCenterConfiguration, create the default one as specified by UpdateCenterConfiguration
                sites.add(defaultSite);
            }
        }
    }

    private XmlFile getConfigFile() {
        return new XmlFile(XSTREAM,new File(Hudson.getInstance().root,
                                    UpdateCenter.class.getName()+".xml"));
    }

    public List<Plugin> getAvailables() {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (UpdateSite s : sites) {
            plugins.addAll(s.getAvailables());
        }

        return plugins;
    }

    /**
     * Returns a list of plugins that should be shown in the "available" tab, grouped by category.
     * A plugin with multiple categories will appear multiple times in the list.
     */
    public PluginEntry[] getCategorizedAvailables() {
        TreeSet<PluginEntry> entries = new TreeSet<PluginEntry>();
        for (Plugin p : getAvailables()) {
            if (p.categories==null || p.categories.length==0)
                entries.add(new PluginEntry(p, getCategoryDisplayName(null)));
            else
                for (String c : p.categories)
                    entries.add(new PluginEntry(p, getCategoryDisplayName(c)));
        }
        return entries.toArray(new PluginEntry[entries.size()]);
    }

    private static String getCategoryDisplayName(String category) {
        if (category==null)
            return Messages.UpdateCenter_PluginCategory_misc();
        try {
            return (String)Messages.class.getMethod(
                    "UpdateCenter_PluginCategory_" + category.replace('-', '_')).invoke(null);
        } catch (Exception ex) {
            return Messages.UpdateCenter_PluginCategory_unrecognized(category);
        }
    }

    public List<Plugin> getUpdates() {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (UpdateSite s : sites) {
            plugins.addAll(s.getUpdates());
        }

        return plugins;
    }


    /**
     * {@link AdministrativeMonitor} that checks if there's Hudson update.
     */
    @Extension
    public static final class CoreUpdateMonitor extends AdministrativeMonitor {
        public boolean isActivated() {
            Data data = getData();
            return data!=null && data.hasCoreUpdates();
        }

        public Data getData() {
            UpdateSite cs = Hudson.getInstance().getUpdateCenter().getCoreSource();
            if (cs!=null)   return cs.getData();
            return null;
        }
    }


    /**
     * Strategy object for controlling the update center's behaviors.
     *
     * <p>
     * Until 1.333, this extension point used to control the configuration of
     * where to get updates (hence the name of this class), but with the introduction
     * of multiple update center sites capability, that functionality is achieved by
     * simply installing another {@link UpdateSite}.
     *
     * <p>
     * See {@link UpdateSite} for how to manipulate them programmatically.
     *
     * @since 1.266
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static class UpdateCenterConfiguration implements ExtensionPoint {
        /**
         * Creates default update center configuration - uses settings for global update center.
         */
        public UpdateCenterConfiguration() {
        }

        /**
         * Check network connectivity by trying to establish a connection to
         * the host in connectionCheckUrl.
         *
         * @param job The connection checker that is invoking this strategy.
         * @param connectionCheckUrl A string containing the URL of a domain
         *          that is assumed to be always available.
         * @throws IOException if a connection can't be established
         */
        public void checkConnection(ConnectionCheckJob job, String connectionCheckUrl) throws IOException {
            testConnection(new URL(connectionCheckUrl));
        }

        /**
         * Check connection to update center server.
         *
         * @param job The connection checker that is invoking this strategy.
         * @param updateCenterUrl A sting containing the URL of the update center host.
         * @throws IOException if a connection to the update center server can't be established.
         */
        public void checkUpdateCenter(ConnectionCheckJob job, String updateCenterUrl) throws IOException {
            testConnection(new URL(updateCenterUrl + "?uctest"));
        }

        /**
         * Validate the URL of the resource before downloading it.
         *
         * @param job The download job that is invoking this strategy. This job is
         *          responsible for managing the status of the download and installation.
         * @param src The location of the resource on the network
         * @throws IOException if the validation fails
         */
        public void preValidate(DownloadJob job, URL src) throws IOException {
        }

        /**
         * Validate the resource after it has been downloaded, before it is
         * installed. The default implementation does nothing.
         *
         * @param job The download job that is invoking this strategy. This job is
         *          responsible for managing the status of the download and installation.
         * @param src The location of the downloaded resource.
         * @throws IOException if the validation fails.
         */
        public void postValidate(DownloadJob job, File src) throws IOException {
        }

        /**
         * Download a plugin or core upgrade in preparation for installing it
         * into its final location. Implementations will normally download the
         * resource into a temporary location and hand off a reference to this
         * location to the install or upgrade strategy to move into the final location.
         *
         * @param job The download job that is invoking this strategy. This job is
         *          responsible for managing the status of the download and installation.
         * @param src The URL to the resource to be downloaded.
         * @return A File object that describes the downloaded resource.
         * @throws IOException if there were problems downloading the resource.
         * @see DownloadJob
         */
        public File download(DownloadJob job, URL src) throws IOException {
            URLConnection con = connect(job,src);
            int total = con.getContentLength();
            CountingInputStream in = new CountingInputStream(con.getInputStream());
            byte[] buf = new byte[8192];
            int len;

            File dst = job.getDestination();
            File tmp = new File(dst.getPath()+".tmp");
            OutputStream out = new FileOutputStream(tmp);

            LOGGER.info("Downloading "+job.getName());
            try {
                while((len=in.read(buf))>=0) {
                    out.write(buf,0,len);
                    job.status = job.new Installing(total==-1 ? -1 : in.getCount()*100/total);
                }
            } catch (IOException e) {
                throw new IOException2("Failed to load "+src+" to "+tmp,e);
            }

            in.close();
            out.close();

            if (total!=-1 && total!=tmp.length()) {
                // don't know exactly how this happens, but report like
                // http://www.ashlux.com/wordpress/2009/08/14/hudson-and-the-sonar-plugin-fail-maveninstallation-nosuchmethoderror/
                // indicates that this kind of inconsistency can happen. So let's be defensive
                throw new IOException("Inconsistent file length: expected "+total+" but only got "+tmp.length());
            }

            return tmp;
        }

        /**
         * Connects to the given URL for downloading the binary. Useful for tweaking
         * how the connection gets established.
         */
        protected URLConnection connect(DownloadJob job, URL src) throws IOException {
            return ProxyConfiguration.open(src);
        }

        /**
         * Called after a plugin has been downloaded to move it into its final
         * location. The default implementation is a file rename.
         *
         * @param job The install job that is invoking this strategy.
         * @param src The temporary location of the plugin.
         * @param dst The final destination to install the plugin to.
         * @throws IOException if there are problems installing the resource.
         */
        public void install(DownloadJob job, File src, File dst) throws IOException {
            job.replace(dst, src);
        }

        /**
         * Called after an upgrade has been downloaded to move it into its final
         * location. The default implementation is a file rename.
         *
         * @param job The upgrade job that is invoking this strategy.
         * @param src The temporary location of the upgrade.
         * @param dst The final destination to install the upgrade to.
         * @throws IOException if there are problems installing the resource.
         */
        public void upgrade(DownloadJob job, File src, File dst) throws IOException {
            job.replace(dst, src);
        }

        /**
         * Returns an "always up" server for Internet connectivity testing.
         *
         * @deprecated as of 1.333
         *      With the introduction of multiple update center capability, this information
         *      is now a part of the <tt>update-center.json</tt> file. See
         *      <tt>http://hudson-ci.org/update-center.json</tt> as an example.
         */
        public String getConnectionCheckUrl() {
            return "http://www.google.com";
        }

        /**
         * Returns the URL of the server that hosts the update-center.json
         * file.
         *
         * @deprecated as of 1.333
         *      With the introduction of multiple update center capability, this information
         *      is now moved to {@link UpdateSite}.
         * @return
         *      Absolute URL that ends with '/'.
         */
        public String getUpdateCenterUrl() {
            return "http://hudson-ci.org/";
        }

        /**
         * Returns the URL of the server that hosts plugins and core updates.
         *
         * @deprecated as of 1.333
         *      <tt>update-center.json</tt> is now signed, so we don't have to further make sure that
         *      we aren't downloading from anywhere unsecure.
         */
        public String getPluginRepositoryBaseUrl() {
            return "http://hudson-ci.org/";
        }


        private void testConnection(URL url) throws IOException {
            try {
                Util.copyStreamAndClose(ProxyConfiguration.open(url).getInputStream(),new NullOutputStream());
            } catch (SSLHandshakeException e) {
                if (e.getMessage().contains("PKIX path building failed"))
                   // fix up this crappy error message from JDK
                    throw new IOException2("Failed to validate the SSL certificate of "+url,e);
            }
        }
    }

    /**
     * Things that {@link UpdateCenter#installerService} executes.
     *
     * This object will have the <tt>row.jelly</tt> which renders the job on UI.
     */
    public abstract class UpdateCenterJob implements Runnable {
        /**
         * Which {@link UpdateSite} does this belong to?
         */
        //TODO: review and check whether we can do it private
        public final UpdateSite site;

        protected UpdateCenterJob(UpdateSite site) {
            this.site = site;
        }

        public UpdateSite getSite() {
            return site;
        }

        /**
         * @deprecated as of 1.326
         *      Use {@link #submit()} instead.
         */
        public void schedule() {
            submit();
        }

        /**
         * Schedules this job for an execution
         * @return
         *      {@link Future} to keeps track of the status of the execution.
         */
        public Future<UpdateCenterJob> submit() {
            LOGGER.fine("Scheduling "+this+" to installerService");
            jobs.add(this);
            return installerService.submit(this,this);
        }
    }

    /**
     * Tests the internet connectivity.
     */
    public final class ConnectionCheckJob extends UpdateCenterJob {
        private final Vector<String> statuses= new Vector<String>();

        public ConnectionCheckJob(UpdateSite site) {
            super(site);
        }

        public void run() {
            LOGGER.fine("Doing a connectivity check");
            try {
                String connectionCheckUrl = site.getConnectionCheckUrl();
                if (connectionCheckUrl!=null) {
                    statuses.add(Messages.UpdateCenter_Status_CheckingInternet());
                    try {
                        config.checkConnection(this, connectionCheckUrl);
                    } catch (IOException e) {
                        if(e.getMessage().contains("Connection timed out")) {
                            // Google can't be down, so this is probably a proxy issue
                            statuses.add(Messages.UpdateCenter_Status_ConnectionFailed(connectionCheckUrl));
                            return;
                        }
                    }
                }

                statuses.add(Messages.UpdateCenter_Status_CheckingJavaNet());
                config.checkUpdateCenter(this, site.getUrl());

                statuses.add(Messages.UpdateCenter_Status_Success());
            } catch (UnknownHostException e) {
                statuses.add(Messages.UpdateCenter_Status_UnknownHostException(e.getMessage()));
                addStatus(e);
            } catch (IOException e) {
                statuses.add(Functions.printThrowable(e));
            }
        }

        private void addStatus(UnknownHostException e) {
            statuses.add("<pre>"+ Functions.xmlEscape(Functions.printThrowable(e))+"</pre>");
        }

        public String[] getStatuses() {
            synchronized (statuses) {
                return statuses.toArray(new String[statuses.size()]);
            }
        }
    }

    /**
     * Base class for a job that downloads a file from the Hudson project.
     */
    public abstract class DownloadJob extends UpdateCenterJob {
        /**
         * Unique ID that identifies this job.
         */
        //TODO: review and check whether we can do it private
        public final int id = iota.incrementAndGet();
        /**
         * Immutable object representing the current state of this job.
         */
        //TODO: review and check whether we can do it private
        public volatile InstallationStatus status = new Pending();

        /**
         * Where to download the file from.
         */
        protected abstract URL getURL() throws MalformedURLException;

        /**
         * Where to download the file to.
         */
        protected abstract File getDestination();

        public abstract String getName();

        /**
         * Called when the whole thing went successfully.
         */
        protected abstract void onSuccess();


        private Authentication authentication;

        /**
         * Get the user that initiated this job
         */
        public Authentication getUser()
        {
            return this.authentication;
        }

        public int getId() {
            return id;
        }

        public InstallationStatus getStatus() {
            return status;
        }

        protected DownloadJob(UpdateSite site, Authentication authentication) {
            super(site);
            this.authentication = authentication;
        }

        public void run() {
            try {
                LOGGER.info("Starting the installation of "+getName()+" on behalf of "+getUser().getName());

                _run();

                LOGGER.info("Installation successful: "+getName());
                status = new Success();
                onSuccess();
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Failed to install "+getName(),e);
                status = new Failure(e);
            }
        }

        protected void _run() throws IOException {
            URL src = getURL();

            config.preValidate(this, src);

            File dst = getDestination();
            File tmp = config.download(this, src);

            config.postValidate(this, tmp);
            config.install(this, tmp, dst);
        }

        /**
         * Called when the download is completed to overwrite
         * the old file with the new file.
         */
        protected void replace(File dst, File src) throws IOException {
            File bak = Util.changeExtension(dst,".bak");
            bak.delete();
            dst.renameTo(bak);
            dst.delete(); // any failure up to here is no big deal
            if(!src.renameTo(dst)) {
                throw new IOException("Failed to rename "+src+" to "+dst);
            }
        }

        /**
         * Indicates the status or the result of a plugin installation.
         * <p>
         * Instances of this class is immutable.
         */
        public abstract class InstallationStatus {
            //TODO: review and check whether we can do it private
            public final int id = iota.incrementAndGet();

            public int getId() {
                return id;
            }

            public boolean isSuccess() {
                return false;
            }
        }

        /**
         * Indicates that the installation of a plugin failed.
         */
        public class Failure extends InstallationStatus {
            //TODO: review and check whether we can do it private
            public final Throwable problem;

            public Failure(Throwable problem) {
                this.problem = problem;
            }

            public Throwable getProblem() {
                return problem;
            }

            public String getStackTrace() {
                return Functions.printThrowable(problem);
            }
        }

        /**
         * Indicates that the plugin was successfully installed.
         */
        public class Success extends InstallationStatus {
            @Override public boolean isSuccess() {
                return true;
            }
        }

        /**
         * Indicates that the plugin is waiting for its turn for installation.
         */
        public class Pending extends InstallationStatus {
        }

        /**
         * Installation of a plugin is in progress.
         */
        public class Installing extends InstallationStatus {
            /**
             * % completed download, or -1 if the percentage is not known.
             */
            //TODO: review and check whether we can do it private
            public final int percentage;

            public int getPercentage() {
                return percentage;
            }

            public Installing(int percentage) {
                this.percentage = percentage;
            }
        }
    }

    /**
     * Represents the state of the installation activity of one plugin.
     */
    public final class InstallationJob extends DownloadJob {
        /**
         * What plugin are we trying to install?
         */
        //TODO: review and check whether we can do it private
        public final Plugin plugin;

        private final PluginManager pm = Hudson.getInstance().getPluginManager();

        public InstallationJob(Plugin plugin, UpdateSite site, Authentication auth) {
            super(site, auth);
            this.plugin = plugin;
        }

        public Plugin getPlugin() {
            return plugin;
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(plugin.url);
        }

        protected File getDestination() {
            File baseDir = pm.rootDir;
            return new File(baseDir, plugin.name + ".hpi");
        }

        public String getName() {
            return plugin.getDisplayName();
        }

        @Override
        public void _run() throws IOException {
            super._run();

            // if this is a bundled plugin, make sure it won't get overwritten
            PluginWrapper pw = plugin.getInstalled();
            if (pw!=null && pw.isBundled())
                try {
                    SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
                    pw.doPin();
                } finally {
                    SecurityContextHolder.clearContext();
                }
        }

        protected void onSuccess() {
            pm.pluginUploaded = true;
        }

        @Override
        public String toString() {
            return super.toString()+"[plugin="+plugin.title+"]";
        }
    }

    /**
     * Represents the state of the downgrading activity of plugin.
     */
    public final class PluginDowngradeJob extends DownloadJob {
        /**
         * What plugin are we trying to install?
         */
        //TODO: review and check whether we can do it private
        public final Plugin plugin;

        private final PluginManager pm = Hudson.getInstance().getPluginManager();

        public PluginDowngradeJob(Plugin plugin, UpdateSite site, Authentication auth) {
            super(site, auth);
            this.plugin = plugin;
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(plugin.url);
        }

        protected File getDestination() {
            File baseDir = pm.rootDir;
            return new File(baseDir, plugin.name + ".hpi");
        }

        protected File getBackup()
        {
            File baseDir = pm.rootDir;
            return new File(baseDir, plugin.name + ".bak");
        }

        public String getName() {
            return plugin.getDisplayName();
        }

        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting the downgrade of "+getName()+" on behalf of "+getUser().getName());

                _run();

                LOGGER.info("Downgrade successful: "+getName());
                status = new Success();
                onSuccess();
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Failed to downgrade "+getName(),e);
                status = new Failure(e);
            }
        }

        @Override
        protected void _run() throws IOException {
            File dst = getDestination();
            File backup = getBackup();

            config.install(this, backup, dst);
        }

        /**
         * Called to overwrite
         * current version with backup file
         */
        @Override
        protected void replace(File dst, File backup) throws IOException {
            dst.delete(); // any failure up to here is no big deal
            if(!backup.renameTo(dst)) {
                throw new IOException("Failed to rename "+backup+" to "+dst);
            }
        }

        protected void onSuccess() {
            pm.pluginUploaded = true;
        }

        @Override
        public String toString() {
            return super.toString()+"[plugin="+plugin.title+"]";
        }
    }

    /**
     * Represents the state of the upgrade activity of Hudson core.
     */
    public final class HudsonUpgradeJob extends DownloadJob {
        public HudsonUpgradeJob(UpdateSite site, Authentication auth) {
            super(site, auth);
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(site.getData().core.url);
        }

        protected File getDestination() {
            return Lifecycle.get().getHudsonWar();
        }

        public String getName() {
            return "hudson.war";
        }

        protected void onSuccess() {
            status = new Success();
        }

        @Override
        protected void replace(File dst, File src) throws IOException {
            Lifecycle.get().rewriteHudsonWar(src);
        }
    }

    public final class HudsonDowngradeJob extends DownloadJob {
        public HudsonDowngradeJob(UpdateSite site, Authentication auth) {
            super(site, auth);
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(site.getData().core.url);
        }

        protected File getDestination() {
            return Lifecycle.get().getHudsonWar();
        }

        public String getName() {
            return "hudson.war";
        }
        protected void onSuccess() {
            status = new Success();
        }
        @Override
        public void run() {
            try {
                LOGGER.info("Starting the downgrade of "+getName()+" on behalf of "+getUser().getName());

                _run();

                LOGGER.info("Downgrading successful: "+getName());
                status = new Success();
                onSuccess();
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Failed to downgrade "+getName(),e);
                status = new Failure(e);
            }
        }

        @Override
        protected void _run() throws IOException {

            File backup = new File(Lifecycle.get().getHudsonWar() + ".bak");
            File dst = getDestination();

            config.install(this, backup, dst);
        }

        @Override
        protected void replace(File dst, File src) throws IOException {
            Lifecycle.get().rewriteHudsonWar(src);
        }
    }

    public static final class PluginEntry implements Comparable<PluginEntry> {
        //TODO: review and check whether we can do it private
        public Plugin plugin;
        public String category;
        private PluginEntry(Plugin p, String c) { plugin = p; category = c; }

        public Plugin getPlugin() {
            return plugin;
        }

        public String getCategory() {
            return category;
        }

        public int compareTo(PluginEntry o) {
            int r = category.compareTo(o.category);
            if (r==0) r = plugin.name.compareToIgnoreCase(o.plugin.name);
            return r;
        }
    }

    /**
     * Adds the update center data retriever to HTML.
     */
    @Extension
    public static class PageDecoratorImpl extends PageDecorator {
        public PageDecoratorImpl() {
            super(PageDecoratorImpl.class);
        }
    }

    /**
     * Initializes the update center.
     *
     * This has to wait until after all plugins load, to let custom UpdateCenterConfiguration take effect first.
     */
    @Initializer(after=PLUGINS_STARTED)
    public static void init(Hudson h) throws IOException {
        h.getUpdateCenter().load();
    }

    /**
     * Sequence number generator.
     */
    private static final AtomicInteger iota = new AtomicInteger();

    private static final Logger LOGGER = Logger.getLogger(UpdateCenter.class.getName());

    /**
     * @deprecated as of 1.333
     *      Use {@link UpdateSite#neverUpdate}
     */
    public static boolean neverUpdate = Boolean.getBoolean(UpdateCenter.class.getName()+".never");

    public static final XStream2 XSTREAM = new XStream2();

    static {
        XSTREAM.alias("site",UpdateSite.class);
        XSTREAM.alias("sites",PersistedList.class);
    }
}
