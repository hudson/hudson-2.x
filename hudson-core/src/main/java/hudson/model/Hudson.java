/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Inc., Kohsuke Kawaguchi, Nikita Levyankov,
 * Erik Ramfelt, Koichi Fujikawa, Red Hat, Inc., Seiji Sogabe,
 * Stephen Connolly, Tom Huybrechts, Yahoo! Inc., Alan Harder, CloudBees, Inc.
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

import antlr.ANTLRException;
import com.thoughtworks.xstream.XStream;
import hudson.BulkChange;
import hudson.DNSMultiCast;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Functions;
import hudson.GlobalMessage;
import hudson.Launcher;
import hudson.Launcher.LocalLauncher;
import hudson.LocalPluginManager;
import hudson.Lookup;
import hudson.markup.MarkupFormatter;
import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import hudson.StructuredForm;
import hudson.TcpSlaveAgentListener;
import hudson.UDPBroadcastThread;
import hudson.Util;
import static hudson.Util.fixEmpty;
import static hudson.Util.fixNull;
import hudson.WebAppMain;
import hudson.XmlFile;
import hudson.cli.CLICommand;
import hudson.cli.CliEntryPoint;
import hudson.cli.CliManagerImpl;
import hudson.cli.declarative.CLIMethod;
import hudson.cli.declarative.CLIResolver;
import hudson.init.InitMilestone;
import hudson.init.InitReactorListener;
import hudson.init.InitStrategy;
import hudson.lifecycle.Lifecycle;
import hudson.logging.LogRecorderManager;
import hudson.lifecycle.RestartNotSupportedException;
import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Descriptor.FormException;
import hudson.model.labels.LabelAtom;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.SCMListener;
import hudson.model.listeners.SaveableListener;
import hudson.remoting.Channel;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.search.CollectionSearchIndex;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.AuthorizationStrategy;
import hudson.security.BasicAuthenticationFilter;
import hudson.security.FederatedLoginService;
import hudson.security.HudsonFilter;
import hudson.security.LegacyAuthorizationStrategy;
import hudson.security.LegacySecurityRealm;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.SecurityMode;
import hudson.security.SecurityRealm;
import hudson.security.csrf.CrumbIssuer;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerListener;
import hudson.slaves.DumbSlave;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodeList;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.NodeProvisioner;
import hudson.slaves.OfflineCause;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Mailer;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.AdministrativeError;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.ClockDifference;
import hudson.util.CopyOnWriteList;
import hudson.util.CopyOnWriteMap;
import hudson.util.DaemonThreadFactory;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import hudson.util.Futures;
import hudson.util.HudsonIsLoading;
import hudson.util.HudsonIsRestarting;
import hudson.util.Iterators;
import hudson.util.Memoizer;
import hudson.util.MultipartFormDataParser;
import hudson.util.RemotingDiagnostics;
import hudson.util.RemotingDiagnostics.HeapDump;
import hudson.util.StreamTaskListener;
import hudson.util.TextFile;
import hudson.util.VersionNumber;
import hudson.util.XStream2;
import hudson.util.Service;
import hudson.views.DefaultMyViewsTabBar;
import hudson.views.DefaultViewsTabBar;
import hudson.views.MyViewsTabBar;
import hudson.views.ViewsTabBar;
import hudson.widgets.Widget;
import net.sf.json.JSONObject;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.AcegiSecurityException;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.ui.AbstractProcessingFilter;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.Task;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorListener;
import org.jvnet.hudson.reactor.TaskGraphBuilder.Handle;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;
import org.xml.sax.InputSource;

import javax.crypto.SecretKey;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static hudson.init.InitMilestone.*;
import hudson.stapler.WebAppController;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Root object of the system.
 *
 * @author Kohsuke Kawaguchi
 * @author Nikita Levyankov
 */
@ExportedBean
public final class Hudson extends Node implements ItemGroup<TopLevelItem>, StaplerProxy, StaplerFallback, ViewGroup, AccessControlled, DescriptorByNameOwner {

    private transient final Queue queue;
    /**
     * Stores various objects scoped to {@link Hudson}.
     */
    public transient final Lookup lookup = new Lookup();
    /**
     * {@link Computer}s in this Hudson system. Read-only.
     */
    private transient final Map<Node, Computer> computers = new CopyOnWriteMap.Hash<Node, Computer>();
    /**
     * We update this field to the current version of Hudson whenever we save {@code config.xml}.
     * This can be used to detect when an upgrade happens from one version to next.
     *
     * <p>
     * Since this field is introduced starting 1.301, "1.0" is used to represent every version
     * up to 1.300. This value may also include non-standard versions like "1.301-SNAPSHOT" or
     * "?", etc., so parsing needs to be done with a care.
     *
     * @since 1.301
     */
    // this field needs to be at the very top so that other components can look at this value even during unmarshalling
    private String version = "1.0";
    /**
     * Number of executors of the master node.
     */
    private int numExecutors = 2;
    /**
     * Job allocation strategy.
     */
    private Mode mode = Mode.NORMAL;
    /**
     * False to enable anyone to do anything.
     * Left as a field so that we can still read old data that uses this flag.
     *
     * @see #authorizationStrategy
     * @see #securityRealm
     */
    private Boolean useSecurity;
    /**
     * Controls how the
     * <a href="http://en.wikipedia.org/wiki/Authorization">authorization</a>
     * is handled in Hudson.
     * <p>
     * This ultimately controls who has access to what.
     *
     * Never null.
     */
    private volatile AuthorizationStrategy authorizationStrategy = AuthorizationStrategy.UNSECURED;
    /**
     * Controls a part of the
     * <a href="http://en.wikipedia.org/wiki/Authentication">authentication</a>
     * handling in Hudson.
     * <p>
     * Intuitively, this corresponds to the user database.
     *
     * See {@link HudsonFilter} for the concrete authentication protocol.
     *
     * Never null. Always use {@link #setSecurityRealm(SecurityRealm)} to
     * update this field.
     *
     * @see #getSecurity()
     * @see #setSecurityRealm(SecurityRealm)
     */
    private volatile SecurityRealm securityRealm = SecurityRealm.NO_AUTHENTICATION;
    /**
     * Message displayed in the top page.
     */
    private String systemMessage;
    private MarkupFormatter markupFormatter;
    private static transient final String HUDSON_WORKSPACES_PROPERTY_KEY = "HUDSON_WORKSPACES";
    /**
     * Workspace root dir which could be configured by setting HUDSON_WORKSPACES property.
     */
    private volatile String configuredWorkspaceRoot;
    /**
     * Root directory of the system.
     */
    public transient final File root;
    /**
     * Where are we in the initialization?
     */
    private transient volatile InitMilestone initLevel = InitMilestone.STARTED;
    /**
     * All {@link Item}s keyed by their {@link Item#getName() name}s.
     */
    /*package*/ transient final Map<String, TopLevelItem> items = new CopyOnWriteMap.Tree<String, TopLevelItem>(CaseInsensitiveComparator.INSTANCE);
    /**
     * The sole instance.
     */
    private static Hudson theInstance;
    private transient volatile boolean isQuietingDown;
    private transient volatile boolean terminating;
    private List<JDK> jdks = new ArrayList<JDK>();
    private transient volatile DependencyGraph dependencyGraph;
    /**
     * Currently active Views tab bar.
     */
    private volatile ViewsTabBar viewsTabBar = new DefaultViewsTabBar();
    /**
     * Currently active My Views tab bar.
     */
    private volatile MyViewsTabBar myViewsTabBar = new DefaultMyViewsTabBar();
    /**
     * All {@link ExtensionList} keyed by their {@link ExtensionList#extensionType}.
     */
    private transient final Memoizer<Class, ExtensionList> extensionLists = new Memoizer<Class, ExtensionList>() {

        public ExtensionList compute(Class key) {
            return ExtensionList.create(Hudson.this, key);
        }
    };
    /**
     * All {@link DescriptorExtensionList} keyed by their {@link DescriptorExtensionList#describableType}.
     */
    private transient final Memoizer<Class, DescriptorExtensionList> descriptorLists = new Memoizer<Class, DescriptorExtensionList>() {

        public DescriptorExtensionList compute(Class key) {
            return DescriptorExtensionList.createDescriptorList(Hudson.this, key);
        }
    };
    /**
     * Active {@link Cloud}s.
     */
    public final CloudList clouds = new CloudList(this);

    public static class CloudList extends DescribableList<Cloud, Descriptor<Cloud>> {

        public CloudList(Hudson h) {
            super(h);
        }

        public CloudList() {// needed for XStream deserialization
        }

        public Cloud getByName(String name) {
            for (Cloud c : this) {
                if (c.name.equals(name)) {
                    return c;
                }
            }
            return null;
        }

        @Override
        protected void onModified() throws IOException {
            super.onModified();
            Hudson.getInstance().trimLabels();
        }
    }
    /**
     * Set of installed cluster nodes.
     * <p>
     * We use this field with copy-on-write semantics.
     * This field has mutable list (to keep the serialization look clean),
     * but it shall never be modified. Only new completely populated slave
     * list can be set here.
     * <p>
     * The field name should be really {@code nodes}, but again the backward compatibility
     * prevents us from renaming.
     */
    private volatile NodeList slaves;
    /**
     * Quiet period.
     *
     * This is {@link Integer} so that we can initialize it to '5' for upgrading users.
     */
    /*package*/ Integer quietPeriod;
    /**
     * Global default for {@link AbstractProject#getScmCheckoutRetryCount()}
     */
    /*package*/ int scmCheckoutRetryCount;
    /**
     * {@link View}s.
     */
    private final CopyOnWriteArrayList<View> views = new CopyOnWriteArrayList<View>();
    /**
     * Name of the primary view.
     * <p>
     * Start with null, so that we can upgrade pre-1.269 data well.
     * @since 1.269
     */
    private volatile String primaryView;
    private transient final FingerprintMap fingerprintMap = new FingerprintMap();
    /**
     * Loaded plugins.
     */
    public transient final PluginManager pluginManager;
    public transient volatile TcpSlaveAgentListener tcpSlaveAgentListener;
    private transient UDPBroadcastThread udpBroadcastThread;
    private transient DNSMultiCast dnsMultiCast;
    /**
     * List of registered {@link ItemListener}s.
     * @deprecated as of 1.286
     */
    private transient final CopyOnWriteList<ItemListener> itemListeners = ExtensionListView.createCopyOnWriteList(ItemListener.class);
    /**
     * List of registered {@link SCMListener}s.
     */
    private transient final CopyOnWriteList<SCMListener> scmListeners = new CopyOnWriteList<SCMListener>();
    /**
     * List of registered {@link ComputerListener}s.
     * @deprecated as of 1.286
     */
    private transient final CopyOnWriteList<ComputerListener> computerListeners = ExtensionListView.createCopyOnWriteList(ComputerListener.class);
    /**
     * TCP slave agent port.
     * 0 for random, -1 to disable.
     */
    private int slaveAgentPort = 0;
    /**
     * Whitespace-separated labels assigned to the master as a {@link Node}.
     */
    private String label = "";
    /**
     * {@link hudson.security.csrf.CrumbIssuer}
     */
    private volatile CrumbIssuer crumbIssuer;
    /**
     * All labels known to Hudson. This allows us to reuse the same label instances
     * as much as possible, even though that's not a strict requirement.
     */
    private transient final ConcurrentHashMap<String, Label> labels = new ConcurrentHashMap<String, Label>();
    /**
     * Load statistics of the entire system.
     */
    @Exported
    public transient final OverallLoadStatistics overallLoad = new OverallLoadStatistics();
    /**
     * {@link NodeProvisioner} that reacts to {@link OverallLoadStatistics}.
     */
    public transient final NodeProvisioner overallNodeProvisioner = new NodeProvisioner(null, overallLoad);
    public transient final ServletContext servletContext;
    /**
     * Transient action list. Useful for adding navigation items to the navigation bar
     * on the left.
     */
    private transient final List<Action> actions = new CopyOnWriteArrayList<Action>();
    /**
     * List of master node properties
     */
    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = new DescribableList<NodeProperty<?>, NodePropertyDescriptor>(this);
    /**
     * List of global properties
     */
    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties = new DescribableList<NodeProperty<?>, NodePropertyDescriptor>(this);
    /**
     * {@link AdministrativeMonitor}s installed on this system.
     *
     * @see AdministrativeMonitor
     */
    public transient final List<AdministrativeMonitor> administrativeMonitors = getExtensionList(AdministrativeMonitor.class);

    /*package*/ final CopyOnWriteArraySet<String> disabledAdministrativeMonitors = new CopyOnWriteArraySet<String>();
    /**
     * Widgets on Hudson.
     */
    private transient final List<Widget> widgets = getExtensionList(Widget.class);
    /**
     * {@link AdjunctManager}
     */
    private transient final AdjunctManager adjuncts;
    /**
     * Code that handles {@link ItemGroup} work.
     */
    private transient final ItemGroupMixIn itemGroupMixIn = new ItemGroupMixIn(this, this) {

        @Override
        protected void add(TopLevelItem item) {
            items.put(item.getName(), item);
        }

        @Override
        protected File getRootDirFor(String name) {
            return Hudson.this.getRootDirFor(name);
        }

        /**
         *send the browser to the config page
         * use View to trim view/{default-view} from URL if possible
         */
        @Override
        protected String redirectAfterCreateItem(StaplerRequest req, TopLevelItem result) throws IOException {
            String redirect = result.getUrl() + "configure";
            List<Ancestor> ancestors = req.getAncestors();
            for (int i = ancestors.size() - 1; i >= 0; i--) {
                Object o = ancestors.get(i).getObject();
                if (o instanceof View) {
                    redirect = req.getContextPath() + '/' + ((View) o).getUrl() + redirect;
                    break;
                }
            }
            return redirect;
        }
    };

    @CLIResolver
    public static Hudson getInstance() {
        return theInstance;
    }
    /**
     * Secrete key generated once and used for a long time, beyond
     * container start/stop. Persisted outside <tt>config.xml</tt> to avoid
     * accidental exposure.
     */
    private transient final String secretKey;
    private transient final UpdateCenter updateCenter = new UpdateCenter();
    /**
     * True if the user opted out from the statistics tracking. We'll never send anything if this is true.
     */
    private Boolean noUsageStatistics;
    /**
     * HTTP proxy configuration.
     */
    public transient volatile ProxyConfiguration proxy;
    /**
     * Bound to "/log".
     */
    private transient final LogRecorderManager log = new LogRecorderManager();

    public Hudson(File root, ServletContext context) throws IOException, InterruptedException, ReactorException {
        this(root, context, null);
    }

    /**
     * @param pluginManager
     *      If non-null, use existing plugin manager.  create a new one.
     */
    public Hudson(File root, ServletContext context, PluginManager pluginManager) throws IOException, InterruptedException, ReactorException {
        // As hudson is starting, grant this process full control
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        try {
            this.root = root;
            this.servletContext = context;
            computeVersion(context);
            if (theInstance != null) {
                throw new IllegalStateException("second instance");
            }
            theInstance = this;

            // doing this early allows InitStrategy to set environment upfront
            final InitStrategy is = InitStrategy.get(Thread.currentThread().getContextClassLoader());

            Trigger.timer = new Timer("Hudson cron thread");
            queue = new Queue(CONSISTENT_HASH ? LoadBalancer.CONSISTENT_HASH : LoadBalancer.DEFAULT);

            try {
                dependencyGraph = DependencyGraph.EMPTY;
            } catch (InternalError e) {
                if (e.getMessage().contains("window server")) {
                    throw new Error("Looks like the server runs without X. Please specify -Djava.awt.headless=true as JVM option", e);
                }
                throw e;
            }

            // get or create the secret
            TextFile secretFile = new TextFile(new File(Hudson.getInstance().getRootDir(), "secret.key"));
            if (secretFile.exists()) {
                secretKey = secretFile.readTrim();
            } else {
                SecureRandom sr = new SecureRandom();
                byte[] random = new byte[32];
                sr.nextBytes(random);
                secretKey = Util.toHexString(random);
                secretFile.write(secretKey);
            }

            try {
                proxy = ProxyConfiguration.load();
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Failed to load proxy configuration", e);
            }

            if (pluginManager == null) {
                pluginManager = new LocalPluginManager(this);
            }
            this.pluginManager = pluginManager;
            // JSON binding needs to be able to see all the classes from all the plugins
            WebApp.get(servletContext).setClassLoader(pluginManager.uberClassLoader);

            adjuncts = new AdjunctManager(servletContext, pluginManager.uberClassLoader, "adjuncts/" + VERSION_HASH);

            // initialization consists of ...
            executeReactor(is,
                    pluginManager.initTasks(is), // loading and preparing plugins
                    loadTasks(), // load jobs
                    InitMilestone.ordering() // forced ordering among key milestones
                    );

            if (KILL_AFTER_LOAD) {
                System.exit(0);
            }

            if (slaveAgentPort != -1) {
                try {
                    tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
                } catch (BindException e) {
                    new AdministrativeError(getClass().getName() + ".tcpBind",
                            "Failed to listen to incoming slave connection",
                            "Failed to listen to incoming slave connection. <a href='configure'>Change the port number</a> to solve the problem.", e);
                }
            } else {
                tcpSlaveAgentListener = null;
            }

            try {
                udpBroadcastThread = new UDPBroadcastThread(this);
                udpBroadcastThread.start();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Faild to broadcast over UDP", e);
            }
            dnsMultiCast = new DNSMultiCast(this);

            updateComputerList();

            {// master is online now
                Computer c = toComputer();
                if (c != null) {
                    for (ComputerListener cl : ComputerListener.all()) {
                        cl.onOnline(c, StreamTaskListener.fromStdout());
                    }
                }
            }

            for (ItemListener l : ItemListener.all()) {
                l.onLoaded();
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Executes a reactor.
     *
     * @param is
     *      If non-null, this can be consulted for ignoring some tasks. Only used during the initialization of Hudson.
     */
    private void executeReactor(final InitStrategy is, TaskBuilder... builders) throws IOException, InterruptedException, ReactorException {
        Reactor reactor = new Reactor(builders) {

            /**
             * Sets the thread name to the task for better diagnostics.
             */
            @Override
            protected void runTask(Task task) throws Exception {
                if (is != null && is.skipInitTask(task)) {
                    return;
                }

                SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);   // full access in the initialization thread
                String taskName = task.getDisplayName();

                Thread t = Thread.currentThread();
                String name = t.getName();
                if (taskName != null) {
                    t.setName(taskName);
                }
                try {
                    long start = System.currentTimeMillis();
                    super.runTask(task);
                    if (LOG_STARTUP_PERFORMANCE) {
                        LOGGER.info(String.format("Took %dms for %s by %s",
                                System.currentTimeMillis() - start, taskName, name));
                    }
                } finally {
                    t.setName(name);
                    SecurityContextHolder.clearContext();
                }
            }
        };

        ExecutorService es;
        if (PARALLEL_LOAD) {
            es = new ThreadPoolExecutor(
                    TWICE_CPU_NUM, TWICE_CPU_NUM, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory());
        } else {
            es = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
        }
        try {
            reactor.execute(es, buildReactorListener());
        } finally {
            es.shutdownNow();   // upon a successful return the executor queue should be empty. Upon an exception, we want to cancel all pending tasks
        }
    }

    /**
     * Aggregates all the listeners into one and returns it.
     *
     * <p>
     * At this point plugins are not loaded yet, so we fall back to the META-INF/services look up to discover implementations.
     * As such there's no way for plugins to participate into this process.
     */
    private ReactorListener buildReactorListener() throws IOException {
        List<ReactorListener> r = (List) Service.loadInstances(Thread.currentThread().getContextClassLoader(), InitReactorListener.class);
        r.add(new ReactorListener() {

            final Level level = Level.parse(System.getProperty(Hudson.class.getName() + ".initLogLevel", "FINE"));

            public void onTaskStarted(Task t) {
                LOGGER.log(level, "Started " + t.getDisplayName());
            }

            public void onTaskCompleted(Task t) {
                LOGGER.log(level, "Completed " + t.getDisplayName());
            }

            public void onTaskFailed(Task t, Throwable err, boolean fatal) {
                LOGGER.log(SEVERE, "Failed " + t.getDisplayName(), err);
            }

            public void onAttained(Milestone milestone) {
                Level lv = level;
                String s = "Attained " + milestone.toString();
                if (milestone instanceof InitMilestone) {
                    lv = Level.INFO; // noteworthy milestones --- at least while we debug problems further
                    initLevel = (InitMilestone) milestone;
                    s = initLevel.toString();
                }
                LOGGER.log(lv, s);
            }
        });
        return new ReactorListener.Aggregator(r);
    }

    public TcpSlaveAgentListener getTcpSlaveAgentListener() {
        return tcpSlaveAgentListener;
    }

    /**
     * Makes {@link AdjunctManager} URL-bound.
     * The dummy parameter allows us to use different URLs for the same adjunct,
     * for proper cache handling.
     */
    public AdjunctManager getAdjuncts(String dummy) {
        return adjuncts;
    }

    @Exported
    public int getSlaveAgentPort() {
        return slaveAgentPort;
    }

    /**
     * If you are calling this on Hudson something is wrong.
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public String getNodeName() {
        return "";
    }

    public void setNodeName(String name) {
        throw new UnsupportedOperationException(); // not allowed
    }

    public String getNodeDescription() {
        return Messages.Hudson_NodeDescription();
    }

    @Exported
    public String getDescription() {
        return systemMessage;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public UpdateCenter getUpdateCenter() {
        return updateCenter;
    }

    public boolean isUsageStatisticsCollected() {
        return noUsageStatistics == null || !noUsageStatistics;
    }

    public void setNoUsageStatistics(Boolean noUsageStatistics) throws IOException {
        this.noUsageStatistics = noUsageStatistics;
        save();
    }

    public View.People getPeople() {
        return new View.People(this);
    }

    /**
     * Does this {@link View} has any associated user information recorded?
     */
    public boolean hasPeople() {
        return View.People.isApplicable(items.values());
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns a secret key that survives across container start/stop.
     * <p>
     * This value is useful for implementing some of the security features.
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Gets {@linkplain #getSecretKey() the secret key} as a key for AES-128.
     * @since 1.308
     */
    public SecretKey getSecretKeyAsAES128() {
        return Util.toAes128Key(secretKey);
    }

    /**
     * Gets the SCM descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<SCM> getScm(String shortClassName) {
        return findDescriptor(shortClassName, SCM.all());
    }

    /**
     * Gets the repository browser descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<RepositoryBrowser<?>> getRepositoryBrowser(String shortClassName) {
        return findDescriptor(shortClassName, RepositoryBrowser.all());
    }

    /**
     * Gets the builder descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<Builder> getBuilder(String shortClassName) {
        return findDescriptor(shortClassName, Builder.all());
    }

    /**
     * Gets the build wrapper descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<BuildWrapper> getBuildWrapper(String shortClassName) {
        return findDescriptor(shortClassName, BuildWrapper.all());
    }

    /**
     * Gets the publisher descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<Publisher> getPublisher(String shortClassName) {
        return findDescriptor(shortClassName, Publisher.all());
    }

    /**
     * Gets the trigger descriptor by name. Primarily used for making them web-visible.
     */
    public TriggerDescriptor getTrigger(String shortClassName) {
        return (TriggerDescriptor) findDescriptor(shortClassName, Trigger.all());
    }

    /**
     * Gets the retention strategy descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<RetentionStrategy<?>> getRetentionStrategy(String shortClassName) {
        return findDescriptor(shortClassName, RetentionStrategy.all());
    }

    /**
     * Gets the {@link JobPropertyDescriptor} by name. Primarily used for making them web-visible.
     */
    public JobPropertyDescriptor getJobProperty(String shortClassName) {
        // combining these two lines triggers javac bug. See issue #610.
        Descriptor d = findDescriptor(shortClassName, JobPropertyDescriptor.all());
        return (JobPropertyDescriptor) d;
    }

    /**
     * Exposes {@link Descriptor} by its name to URL.
     *
     * After doing all the {@code getXXX(shortClassName)} methods, I finally realized that
     * this just doesn't scale.
     *
     * @param id
     *      Either {@link Descriptor#getId()} (recommended) or the short name of a {@link Describable} subtype (for compatibility)
     */
    public Descriptor getDescriptor(String id) {
        // legacy descriptors that are reigstered manually doesn't show up in getExtensionList, so check them explicitly.
        for (Descriptor d : Iterators.sequence(getExtensionList(Descriptor.class), DescriptorExtensionList.listLegacyInstances())) {
            String name = d.getId();
            if (name.equals(id)) {
                return d;
            }
            if (name.substring(name.lastIndexOf('.') + 1).equals(id)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Alias for {@link #getDescriptor(String)}.
     */
    public Descriptor getDescriptorByName(String id) {
        return getDescriptor(id);
    }

    /**
     * Gets the {@link Descriptor} that corresponds to the given {@link Describable} type.
     * <p>
     * If you have an instance of {@code type} and call {@link Describable#getDescriptor()},
     * you'll get the same instance that this method returns.
     */
    public Descriptor getDescriptor(Class<? extends Describable> type) {
        for (Descriptor d : getExtensionList(Descriptor.class)) {
            if (d.clazz == type) {
                return d;
            }
        }
        return null;
    }

    /**
     * Works just like {@link #getDescriptor(Class)} but don't take no for an answer.
     *
     * @throws AssertionError
     *      If the descriptor is missing.
     * @since 1.326
     */
    public Descriptor getDescriptorOrDie(Class<? extends Describable> type) {
        Descriptor d = getDescriptor(type);
        if (d == null) {
            throw new AssertionError(type + " is missing its descriptor");
        }
        return d;
    }

    /**
     * Gets the {@link Descriptor} instance in the current Hudson by its type.
     */
    public <T extends Descriptor> T getDescriptorByType(Class<T> type) {
        for (Descriptor d : getExtensionList(Descriptor.class)) {
            if (d.getClass() == type) {
                return type.cast(d);
            }
        }
        return null;
    }

    /**
     * Gets the {@link SecurityRealm} descriptors by name. Primarily used for making them web-visible.
     */
    public Descriptor<SecurityRealm> getSecurityRealms(String shortClassName) {
        return findDescriptor(shortClassName, SecurityRealm.all());
    }

    /**
     * Finds a descriptor that has the specified name.
     */
    private <T extends Describable<T>> Descriptor<T> findDescriptor(String shortClassName, Collection<? extends Descriptor<T>> descriptors) {
        String name = '.' + shortClassName;
        for (Descriptor<T> d : descriptors) {
            if (d.clazz.getName().endsWith(name)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Gets all the installed {@link ItemListener}s.
     *
     * @deprecated as of 1.286.
     *      Use {@link ItemListener#all()}.
     */
    public CopyOnWriteList<ItemListener> getJobListeners() {
        return itemListeners;
    }

    /**
     * Gets all the installed {@link SCMListener}s.
     */
    public CopyOnWriteList<SCMListener> getSCMListeners() {
        return scmListeners;
    }

    /**
     * Gets all the installed {@link ComputerListener}s.
     *
     * @deprecated as of 1.286.
     *      Use {@link ComputerListener#all()}.
     */
    public CopyOnWriteList<ComputerListener> getComputerListeners() {
        return computerListeners;
    }

    /**
     * Gets the plugin object from its short name.
     *
     * <p>
     * This allows URL <tt>hudson/plugin/ID</tt> to be served by the views
     * of the plugin class.
     */
    public Plugin getPlugin(String shortName) {
        PluginWrapper p = pluginManager.getPlugin(shortName);
        if (p == null) {
            return null;
        }
        return p.getPlugin();
    }

    /**
     * Gets the plugin object from its class.
     *
     * <p>
     * This allows easy storage of plugin information in the plugin singleton without
     * every plugin reimplementing the singleton pattern.
     *
     * @param clazz The plugin class (beware class-loader fun, this will probably only work
     * from within the hpi that defines the plugin class, it may or may not work in other cases)
     *
     * @return The plugin instance.
     */
    @SuppressWarnings("unchecked")
    public <P extends Plugin> P getPlugin(Class<P> clazz) {
        PluginWrapper p = pluginManager.getPlugin(clazz);
        if (p == null) {
            return null;
        }
        return (P) p.getPlugin();
    }

    /**
     * Gets the plugin objects from their super-class.
     *
     * @param clazz The plugin class (beware class-loader fun)
     *
     * @return The plugin instances.
     */
    public <P extends Plugin> List<P> getPlugins(Class<P> clazz) {
        List<P> result = new ArrayList<P>();
        for (PluginWrapper w : pluginManager.getPlugins(clazz)) {
            result.add((P) w.getPlugin());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Synonym to {@link #getNodeDescription()}.
     */
    public String getSystemMessage() {
        return systemMessage;
    }

    /**
     * Gets the markup formatter used in the system.
     *
     * @return
     *      never null.
     * @since 1.391
     */
    public MarkupFormatter getMarkupFormatter() {
        return markupFormatter != null ? markupFormatter : RawHtmlMarkupFormatter.INSTANCE;
    }

    /**
     * Sets the markup formatter used in the system globally.
     *
     * @since 1.391
     */
    public void setMarkupFormatter(MarkupFormatter f) {
        this.markupFormatter = f;
    }

    /**
     * Sets the system message.
     */
    public void setSystemMessage(String message) throws IOException {
        this.systemMessage = message;
        save();
    }

    public FederatedLoginService getFederatedLoginService(String name) {
        for (FederatedLoginService fls : FederatedLoginService.all()) {
            if (fls.getUrlName().equals(name)) {
                return fls;
            }
        }
        return null;
    }

    public List<FederatedLoginService> getFederatedLoginServices() {
        return FederatedLoginService.all();
    }

    public Launcher createLauncher(TaskListener listener) {
        return new LocalLauncher(listener).decorateFor(this);
    }
    private final transient Object updateComputerLock = new Object();

    /**
     * Updates {@link #computers} by using {@link #getSlaves()}.
     *
     * <p>
     * This method tries to reuse existing {@link Computer} objects
     * so that we won't upset {@link Executor}s running in it.
     */
    private void updateComputerList() throws IOException {
        synchronized (updateComputerLock) {// just so that we don't have two code updating computer list at the same time
            Map<String, Computer> byName = new HashMap<String, Computer>();
            for (Computer c : computers.values()) {
                if (c.getNode() == null) {
                    continue;   // this computer is gone
                }
                byName.put(c.getNode().getNodeName(), c);
            }

            Set<Computer> old = new HashSet<Computer>(computers.values());
            Set<Computer> used = new HashSet<Computer>();

            updateComputer(this, byName, used);
            for (Node s : getNodes()) {
                updateComputer(s, byName, used);
            }

            // find out what computers are removed, and kill off all executors.
            // when all executors exit, it will be removed from the computers map.
            // so don't remove too quickly
            old.removeAll(used);
            for (Computer c : old) {
                c.kill();
            }
        }
        getQueue().scheduleMaintenance();
        for (ComputerListener cl : ComputerListener.all()) {
            cl.onConfigurationChange();
        }
    }

    private void updateComputer(Node n, Map<String, Computer> byNameMap, Set<Computer> used) {
        Computer c;
        c = byNameMap.get(n.getNodeName());
        if (c != null) {
            c.setNode(n); // reuse
        } else {
            if (n.getNumExecutors() > 0) {
                computers.put(n, c = n.createComputer());
                if (!n.holdOffLaunchUntilSave && AUTOMATIC_SLAVE_LAUNCH) {
                    RetentionStrategy retentionStrategy = c.getRetentionStrategy();
                    if (retentionStrategy != null) {
                        // if there is a retention strategy, it is responsible for deciding to start the computer
                        retentionStrategy.start(c);
                    } else {
                        // we should never get here, but just in case, we'll fall back to the legacy behaviour
                        c.connect(true);
                    }
                }
            }
        }
        used.add(c);
    }

    /*package*/ void removeComputer(Computer computer) {
        for (Entry<Node, Computer> e : computers.entrySet()) {
            if (e.getValue() == computer) {
                computers.remove(e.getKey());
                return;
            }
        }
        throw new IllegalStateException("Trying to remove unknown computer");
    }

    public String getFullName() {
        return "";
    }

    public String getFullDisplayName() {
        return "";
    }

    /**
     * Returns the transient {@link Action}s associated with the top page.
     *
     * <p>
     * Adding {@link Action} is primarily useful for plugins to contribute
     * an item to the navigation bar of the top page. See existing {@link Action}
     * implementation for it affects the GUI.
     *
     * <p>
     * To register an {@link Action}, implement {@link RootAction} extension point, or write code like
     * {@code Hudson.getInstance().getActions().add(...)}.
     *
     * @return
     *      Live list where the changes can be made. Can be empty but never null.
     * @since 1.172
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Gets just the immediate children of {@link Hudson}.
     *
     * @see #getAllItems(Class)
     */
    @Exported(name = "jobs")
    public List<TopLevelItem> getItems() {
        List<TopLevelItem> viewableItems = new ArrayList<TopLevelItem>();
        for (TopLevelItem item : items.values()) {
            if (item.hasPermission(Item.READ)) {
                viewableItems.add(item);
            }
        }

        return viewableItems;
    }

    /**
     * Returns the read-only view of all the {@link TopLevelItem}s keyed by their names.
     * <p>
     * This method is efficient, as it doesn't involve any copying.
     *
     * @since 1.296
     */
    public Map<String, TopLevelItem> getItemMap() {
        return Collections.unmodifiableMap(items);
    }

    /**
     * Gets just the immediate children of {@link Hudson} but of the given type.
     */
    public <T> List<T> getItems(Class<T> type) {
        List<T> r = new ArrayList<T>();
        for (TopLevelItem i : getItems()) {
            if (type.isInstance(i)) {
                r.add(type.cast(i));
            }
        }
        return r;
    }

    /**
     * Gets all the {@link Item}s recursively in the {@link ItemGroup} tree
     * and filter them by the given type.
     */
    public <T extends Item> List<T> getAllItems(Class<T> type) {
        List<T> r = new ArrayList<T>();

        Stack<ItemGroup> q = new Stack<ItemGroup>();
        q.push(this);

        while (!q.isEmpty()) {
            ItemGroup<?> parent = q.pop();
            for (Item i : parent.getItems()) {
                if (type.isInstance(i)) {
                    if (i.hasPermission(Item.READ)) {
                        r.add(type.cast(i));
                    }
                }
                if (i instanceof ItemGroup) {
                    q.push((ItemGroup) i);
                }
            }
        }

        return r;
    }

    /**
     * Gets the list of all the projects.
     *
     * <p>
     * Since {@link Project} can only show up under {@link Hudson},
     * no need to search recursively.
     */
    public List<Project> getProjects() {
        return Util.createSubList(items.values(), Project.class);
    }

    /**
     * Gets the names of all the {@link Job}s.
     */
    public Collection<String> getJobNames() {
        List<String> names = new ArrayList<String>();
        for (Job j : getAllItems(Job.class)) {
            names.add(j.getFullName());
        }
        return names;
    }

    /**
     * Gets the names of all the {@link TopLevelItem}s.
     */
    public Collection<String> getTopLevelItemNames() {
        List<String> names = new ArrayList<String>();
        for (TopLevelItem j : items.values()) {
            names.add(j.getName());
        }
        return names;
    }

    public synchronized View getView(String name) {
        for (View v : views) {
            if (v.getViewName().equals(name)) {
                return v;
            }
        }
        if (name != null && !name.equals(primaryView)) {
            // Fallback to subview of primary view if it is a ViewGroup
            View pv = getPrimaryView();
            if (pv instanceof ViewGroup) {
                return ((ViewGroup) pv).getView(name);
            }
        }
        return null;
    }

    /**
     * Gets the read-only list of all {@link View}s.
     */
    @Exported
    public synchronized Collection<View> getViews() {
        List<View> copy = new ArrayList<View>(views);
        Collections.sort(copy, View.SORTER);
        return copy;
    }

    public void addView(View v) throws IOException {
        v.owner = this;
        views.add(v);
        save();
    }

    public boolean canDelete(View view) {
        return !view.isDefault();  // Cannot delete primary view
    }

    public synchronized void deleteView(View view) throws IOException {
        if (views.size() <= 1) {
            throw new IllegalStateException("Cannot delete last view");
        }
        views.remove(view);
        save();
    }

    public ViewsTabBar getViewsTabBar() {
        return viewsTabBar;
    }

    public MyViewsTabBar getMyViewsTabBar() {
        return myViewsTabBar;
    }

    /**
     * Returns true if the current running Hudson is upgraded from a version earlier than the specified version.
     *
     * <p>
     * This method continues to return true until the system configuration is saved, at which point
     * {@link #version} will be overwritten and Hudson forgets the upgrade history.
     *
     * <p>
     * To handle SNAPSHOTS correctly, pass in "1.N.*" to test if it's upgrading from the version
     * equal or younger than N. So say if you implement a feature in 1.301 and you want to check
     * if the installation upgraded from pre-1.301, pass in "1.300.*"
     *
     * @since 1.301
     */
    public boolean isUpgradedFromBefore(VersionNumber v) {
        try {
            return new VersionNumber(version).isOlderThan(v);
        } catch (IllegalArgumentException e) {
            // fail to parse this version number
            return false;
        }
    }

    /**
     * Gets the read-only list of all {@link Computer}s.
     */
    public Computer[] getComputers() {
        Computer[] r = computers.values().toArray(new Computer[computers.size()]);
        Arrays.sort(r, new Comparator<Computer>() {

            final Collator collator = Collator.getInstance();

            public int compare(Computer lhs, Computer rhs) {
                if (lhs.getNode() == Hudson.this) {
                    return -1;
                }
                if (rhs.getNode() == Hudson.this) {
                    return 1;
                }
                return collator.compare(lhs.getDisplayName(), rhs.getDisplayName());
            }
        });
        return r;
    }

    /*package*/ Computer getComputer(Node n) {
        return computers.get(n);
    }

    @CLIResolver
    public Computer getComputer(@Argument(required = true, metaVar = "NAME", usage = "Node name") String name) {
        if (name.equals("(master)")) {
            name = "";
        }

        for (Computer c : computers.values()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * @deprecated
     *      UI method. Not meant to be used programatically.
     */
    public ComputerSet getComputer() {
        return new ComputerSet();
    }

    /**
     * Gets the label that exists on this system by the name.
     *
     * @return null if name is null.
     * @see Label#parseExpression(String) (String)
     */
    public Label getLabel(String expr) {
        if (expr == null) {
            return null;
        }
        while (true) {
            Label l = labels.get(expr);
            if (l != null) {
                return l;
            }

            // non-existent
            try {
                labels.putIfAbsent(expr, Label.parseExpression(expr));
            } catch (ANTLRException e) {
                // laxly accept it as a single label atom for backward compatibility
                return getLabelAtom(expr);
            }
        }
    }

    /**
     * Returns the label atom of the given name.
     */
    public LabelAtom getLabelAtom(String name) {
        if (name == null) {
            return null;
        }

        while (true) {
            Label l = labels.get(name);
            if (l != null) {
                return (LabelAtom) l;
            }

            // non-existent
            LabelAtom la = new LabelAtom(name);
            if (labels.putIfAbsent(name, la) == null) {
                la.load();
            }
        }
    }

    /**
     * Gets all the active labels in the current system.
     */
    public Set<Label> getLabels() {
        Set<Label> r = new TreeSet<Label>();
        for (Label l : labels.values()) {
            if (!l.isEmpty()) {
                r.add(l);
            }
        }
        return r;
    }

    public Set<LabelAtom> getLabelAtoms() {
        Set<LabelAtom> r = new TreeSet<LabelAtom>();
        for (Label l : labels.values()) {
            if (!l.isEmpty() && l instanceof LabelAtom) {
                r.add((LabelAtom) l);
            }
        }
        return r;
    }

    public Queue getQueue() {
        return queue;
    }

    @Override
    public String getDisplayName() {
        return Messages.Hudson_DisplayName();
    }

    public List<JDK> getJDKs() {
        if (jdks == null) {
            jdks = new ArrayList<JDK>();
        }
        return jdks;
    }

    /**
     * Gets the JDK installation of the given name, or returns null.
     */
    public JDK getJDK(String name) {
        if (name == null) {
            // if only one JDK is configured, "default JDK" should mean that JDK.
            List<JDK> jdks = getJDKs();
            if (jdks.size() == 1) {
                return jdks.get(0);
            }
            return null;
        }
        for (JDK j : getJDKs()) {
            if (j.getName().equals(name)) {
                return j;
            }
        }
        return null;
    }

    /**
     * Gets the slave node of the give name, hooked under this Hudson.
     *
     * @deprecated
     *      Use {@link #getNode(String)}. Since 1.252.
     */
    public Slave getSlave(String name) {
        Node n = getNode(name);
        if (n instanceof Slave) {
            return (Slave) n;
        }
        return null;
    }

    /**
     * Gets the slave node of the give name, hooked under this Hudson.
     */
    public Node getNode(String name) {
        for (Node s : getNodes()) {
            if (s.getNodeName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets a {@link Cloud} by {@link Cloud#name its name}, or null.
     */
    public Cloud getCloud(String name) {
        return clouds.getByName(name);
    }

    /**
     * @deprecated
     *      Use {@link #getNodes()}. Since 1.252.
     */
    public List<Slave> getSlaves() {
        return (List) Collections.unmodifiableList(slaves);
    }

    /**
     * Returns all {@link Node}s in the system, excluding {@link Hudson} instance itself which
     * represents the master.
     */
    public List<Node> getNodes() {
        return Collections.unmodifiableList(slaves);
    }

    /**
     * Updates the slave list.
     *
     * @deprecated
     *      Use {@link #setNodes(List)}. Since 1.252.
     */
    public void setSlaves(List<Slave> slaves) throws IOException {
        setNodes(slaves);
    }

    /**
     * Adds one more {@link Node} to Hudson.
     */
    public synchronized void addNode(Node n) throws IOException {
        if (n == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<Node> nl = new ArrayList<Node>(this.slaves);
        if (!nl.contains(n)) // defensive check
        {
            nl.add(n);
        }
        setNodes(nl);
    }

    /**
     * Removes a {@link Node} from Hudson.
     */
    public synchronized void removeNode(Node n) throws IOException {
        Computer c = n.toComputer();
        if (c != null) {
            c.disconnect(OfflineCause.create(Messages._Hudson_NodeBeingRemoved()));
        }

        ArrayList<Node> nl = new ArrayList<Node>(this.slaves);
        nl.remove(n);
        setNodes(nl);
    }

    public void setNodes(List<? extends Node> nodes) throws IOException {
        // make sure that all names are unique
        Set<String> names = new HashSet<String>();
        for (Node n : nodes) {
            if (!names.add(n.getNodeName())) {
                throw new IllegalArgumentException(n.getNodeName() + " is defined more than once");
            }
        }
        this.slaves = new NodeList(nodes);
        updateComputerList();
        trimLabels();
        save();
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
        return nodeProperties;
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getGlobalNodeProperties() {
        return globalNodeProperties;
    }

    /**
     * Resets all labels and remove invalid ones.
     */
    private void trimLabels() {
        for (Iterator<Label> itr = labels.values().iterator(); itr.hasNext();) {
            Label l = itr.next();
            l.reset();
            if (l.isEmpty()) {
                itr.remove();
            }
        }
    }

    /**
     * Binds {@link AdministrativeMonitor}s to URL.
     */
    public AdministrativeMonitor getAdministrativeMonitor(String id) {
        for (AdministrativeMonitor m : administrativeMonitors) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    public NodeDescriptor getDescriptor() {
        return DescriptorImpl.INSTANCE;
    }

    public static final class DescriptorImpl extends NodeDescriptor {

        @Extension
        public static final DescriptorImpl INSTANCE = new DescriptorImpl();

        public String getDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isInstantiable() {
            return false;
        }

        public FormValidation doCheckNumExecutors(@QueryParameter String value) {
            return FormValidation.validateNonNegativeInteger(value);
        }

        // to route /descriptor/FQCN/xxx to getDescriptor(FQCN).xxx
        public Object getDynamic(String token) {
            return Hudson.getInstance().getDescriptor(token);
        }
    }

    /**
     * Gets the system default quiet period.
     */
    public int getQuietPeriod() {
        return quietPeriod != null ? quietPeriod : 5;
    }

    /**
     * Gets the global SCM check out retry count.
     */
    public int getScmCheckoutRetryCount() {
        return scmCheckoutRetryCount;
    }

    /**
     * @deprecated
     *      Why are you calling a method that always returns ""?
     *      Perhaps you meant {@link #getRootUrl()}.
     */
    public String getUrl() {
        return "";
    }

    @Override
    public String getSearchUrl() {
        return "";
    }

    public void onViewRenamed(View view, String oldName, String newName) {
        // implementation of Hudson is immune to view name change.
    }

    @Override
    public SearchIndexBuilder makeSearchIndex() {
        return super.makeSearchIndex().add("configure", "config", "configure").add("manage").add("log").add(getPrimaryView().makeSearchIndex()).add(new CollectionSearchIndex() {// for computers

            protected Computer get(String key) {
                return getComputer(key);
            }

            protected Collection<Computer> all() {
                return computers.values();
            }
        }).add(new CollectionSearchIndex() {// for users

            protected User get(String key) {
                return User.get(key, false);
            }

            protected Collection<User> all() {
                return User.getAll();
            }
        }).add(new CollectionSearchIndex() {// for views

            protected View get(String key) {
                return getView(key);
            }

            protected Collection<View> all() {
                return views;
            }
        });
    }

    /**
     * Returns the primary {@link View} that renders the top-page of Hudson.
     */
    @Exported
    public View getPrimaryView() {
        View v = getView(primaryView);
        if (v == null) // fallback
        {
            v = views.get(0);
        }
        return v;
    }

    public String getUrlChildPrefix() {
        return "job";
    }

    /**
     * Gets the absolute URL of Hudson,
     * such as "http://localhost/hudson/".
     *
     * <p>
     * This method first tries to use the manually configured value, then
     * fall back to {@link StaplerRequest#getRootPath()}.
     * It is done in this order so that it can work correctly even in the face
     * of a reverse proxy.
     *
     * @return
     *      This method returns null if this parameter is not configured by the user.
     *      The caller must gracefully deal with this situation.
     *      The returned URL will always have the trailing '/'.
     * @since 1.66
     * @see Descriptor#getCheckUrl(String)
     * @see #getRootUrlFromRequest()
     */
    public String getRootUrl() {
        // for compatibility. the actual data is stored in Mailer
        String url = Mailer.descriptor().getUrl();
        if (url != null) {
            return url;
        }

        StaplerRequest req = Stapler.getCurrentRequest();
        if (req != null) {
            return getRootUrlFromRequest();
        }
        return null;
    }

    /**
     * Gets the absolute URL of Hudson top page, such as "http://localhost/hudson/".
     *
     * <p>
     * Unlike {@link #getRootUrl()}, which uses the manually configured value,
     * this one uses the current request to reconstruct the URL. The benefit is
     * that this is immune to the configuration mistake (users often fail to set the root URL
     * correctly, especially when a migration is involved), but the downside
     * is that unless you are processing a request, this method doesn't work.
     *
     * @since 1.263
     */
    public String getRootUrlFromRequest() {
        StaplerRequest req = Stapler.getCurrentRequest();
        StringBuilder buf = new StringBuilder();
        buf.append(req.getScheme() + "://");
        buf.append(req.getServerName());
        if (req.getServerPort() != 80) {
            buf.append(':').append(req.getServerPort());
        }
        buf.append(req.getContextPath()).append('/');
        return buf.toString();
    }

    public File getRootDir() {
        return root;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If {@link Hudson#HUDSON_WORKSPACES_PROPERTY_KEY} is set through JNDI
     * or system properties or environment variables. workspaceRoot will be set based on property value.
     */
    public FilePath getWorkspaceFor(TopLevelItem item) {
        String workspaceRoot = getConfiguredWorkspaceRoot();
        if (StringUtils.isNotBlank(workspaceRoot)) {
            return new FilePath(new File(workspaceRoot + "/" + item.getName(), WORKSPACE_DIRNAME));
        } else {
            return new FilePath(new File(item.getRootDir(), WORKSPACE_DIRNAME));
        }
    }

    /**
     * Checks jndi,environment properties and system properties for {@link Hudson#HUDSON_WORKSPACES_PROPERTY_KEY} value
     *
     * @return configured workspace root or "" if it is not set
     */
    private String getConfiguredWorkspaceRoot() {
        if (null == configuredWorkspaceRoot) {
            String resultValue = getConfiguredHudsonProperty(HUDSON_WORKSPACES_PROPERTY_KEY);
            configuredWorkspaceRoot = (null != resultValue) ? resultValue : StringUtils.EMPTY;
        }
        return configuredWorkspaceRoot;
    }

    public FilePath getRootPath() {
        return new FilePath(getRootDir());
    }

    @Override
    public FilePath createPath(String absolutePath) {
        return new FilePath((VirtualChannel) null, absolutePath);
    }

    public ClockDifference getClockDifference() {
        return ClockDifference.ZERO;
    }

    /**
     * For binding {@link LogRecorderManager} to "/log".
     * Everything below here is admin-only, so do the check here.
     */
    public LogRecorderManager getLog() {
        checkPermission(ADMINISTER);
        return log;
    }

    /**
     * A convenience method to check if there's some security
     * restrictions in place.
     */
    @Exported
    public boolean isUseSecurity() {
        return securityRealm != SecurityRealm.NO_AUTHENTICATION || authorizationStrategy != AuthorizationStrategy.UNSECURED;
    }

    /**
     * If true, all the POST requests to Hudson would have to have crumb in it to protect
     * Hudson from CSRF vulnerabilities.
     */
    @Exported
    public boolean isUseCrumbs() {
        return crumbIssuer != null;
    }

    /**
     * Returns the constant that captures the three basic security modes
     * in Hudson.
     */
    public SecurityMode getSecurity() {
        // fix the variable so that this code works under concurrent modification to securityRealm.
        SecurityRealm realm = securityRealm;

        if (realm == SecurityRealm.NO_AUTHENTICATION) {
            return SecurityMode.UNSECURED;
        }
        if (realm instanceof LegacySecurityRealm) {
            return SecurityMode.LEGACY;
        }
        return SecurityMode.SECURED;
    }

    /**
     * @return
     *      never null.
     */
    public SecurityRealm getSecurityRealm() {
        return securityRealm;
    }

    public void setSecurityRealm(SecurityRealm securityRealm) {
        if (securityRealm == null) {
            securityRealm = SecurityRealm.NO_AUTHENTICATION;
        }
        this.securityRealm = securityRealm;
        // reset the filters and proxies for the new SecurityRealm
        try {
            HudsonFilter filter = HudsonFilter.get(servletContext);
            if (filter == null) {
                // Fix for #3069: This filter is not necessarily initialized before the servlets.
                // when HudsonFilter does come back, it'll initialize itself.
                LOGGER.fine("HudsonFilter has not yet been initialized: Can't perform security setup for now");
            } else {
                LOGGER.fine("HudsonFilter has been previously initialized: Setting security up");
                filter.reset(securityRealm);
                LOGGER.fine("Security is now fully set up");
            }
        } catch (ServletException e) {
            // for binary compatibility, this method cannot throw a checked exception
            throw new AcegiSecurityException("Failed to configure filter", e) {
            };
        }
    }

    public void setAuthorizationStrategy(AuthorizationStrategy a) {
        if (a == null) {
            a = AuthorizationStrategy.UNSECURED;
        }
        authorizationStrategy = a;
    }

    public Lifecycle getLifecycle() {
        return Lifecycle.get();
    }

    /**
     * Returns {@link ExtensionList} that retains the discovered instances for the given extension type.
     *
     * @param extensionType
     *      The base type that represents the extension point. Normally {@link ExtensionPoint} subtype
     *      but that's not a hard requirement.
     * @return
     *      Can be an empty list but never null.
     */
    @SuppressWarnings({"unchecked"})
    public <T> ExtensionList<T> getExtensionList(Class<T> extensionType) {
        return extensionLists.get(extensionType);
    }

    /**
     * Used to bind {@link ExtensionList}s to URLs.
     *
     * @since 1.349
     */
    public ExtensionList getExtensionList(String extensionType) throws ClassNotFoundException {
        return getExtensionList(pluginManager.uberClassLoader.loadClass(extensionType));
    }

    /**
     * Returns {@link ExtensionList} that retains the discovered {@link Descriptor} instances for the given
     * kind of {@link Describable}.
     *
     * @return
     *      Can be an empty list but never null.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorList(Class<T> type) {
        return descriptorLists.get(type);
    }

    /**
     * Returns the root {@link ACL}.
     *
     * @see AuthorizationStrategy#getRootACL()
     */
    @Override
    public ACL getACL() {
        return authorizationStrategy.getRootACL();
    }

    /**
     * @return
     *      never null.
     */
    public AuthorizationStrategy getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    /**
     * Returns true if Hudson is quieting down.
     * <p>
     * No further jobs will be executed unless it
     * can be finished while other current pending builds
     * are still in progress.
     */
    public boolean isQuietingDown() {
        return isQuietingDown;
    }

    /**
     * Returns true if the container initiated the termination of the web application.
     */
    public boolean isTerminating() {
        return terminating;
    }

    /**
     * Gets the initialization milestone that we've already reached.
     *
     * @return
     *      {@link InitMilestone#STARTED} even if the initialization hasn't been started, so that this method
     *      never returns null.
     */
    public InitMilestone getInitLevel() {
        return initLevel;
    }

    public void setNumExecutors(int n) throws IOException {
        this.numExecutors = n;
        save();
    }

    /**
     * @deprecated
     *      Left only for the compatibility of URLs.
     *      Should not be invoked for any other purpose.
     */
    public TopLevelItem getJob(String name) {
        return getItem(name);
    }

    /**
     * @deprecated
     *      Used only for mapping jobs to URL in a case-insensitive fashion.
     */
    public TopLevelItem getJobCaseInsensitive(String name) {
        String match = Functions.toEmailSafeString(name);
        for (Entry<String, TopLevelItem> e : items.entrySet()) {
            if (Functions.toEmailSafeString(e.getKey()).equalsIgnoreCase(match)) {
                TopLevelItem item = e.getValue();
                return item.hasPermission(Item.READ) ? item : null;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}.
     *
     * Note that the look up is case-insensitive.
     */
    public TopLevelItem getItem(String name) {
        TopLevelItem item = items.get(name);
        if (item == null || !item.hasPermission(Item.READ)) {
            return null;
        }
        return item;
    }

    public File getRootDirFor(TopLevelItem child) {
        return getRootDirFor(child.getName());
    }

    private File getRootDirFor(String name) {
        return new File(new File(getRootDir(), "jobs"), name);
    }

    /**
     * Gets the {@link Item} object by its full name.
     * Full names are like path names, where each name of {@link Item} is
     * combined by '/'.
     *
     * @return
     *      null if either such {@link Item} doesn't exist under the given full name,
     *      or it exists but it's no an instance of the given type.
     */
    public <T extends Item> T getItemByFullName(String fullName, Class<T> type) {
        StringTokenizer tokens = new StringTokenizer(fullName, "/");
        ItemGroup parent = this;

        if (!tokens.hasMoreTokens()) {
            return null;    // for example, empty full name.
        }
        while (true) {
            Item item = parent.getItem(tokens.nextToken());
            if (!tokens.hasMoreTokens()) {
                if (type.isInstance(item)) {
                    return type.cast(item);
                } else {
                    return null;
                }
            }

            if (!(item instanceof ItemGroup)) {
                return null;    // this item can't have any children
            }
            parent = (ItemGroup) item;
        }
    }

    public Item getItemByFullName(String fullName) {
        return getItemByFullName(fullName, Item.class);
    }

    /**
     * Gets the user of the given name.
     *
     * @return
     *      This method returns a non-null object for any user name, without validation.
     */
    public User getUser(String name) {
        return User.get(name, false);
    }

    /**
     * Creates a new job.
     *
     * @throws IllegalArgumentException
     *      if the project of the given name already exists.
     */
    public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name) throws IOException {
        return createProject(type, name, true);
    }

    /**
     * Creates a new job.
     * @param type Descriptor for job type
     * @param name Name for job
     * @param notify Whether to fire onCreated method for all ItemListeners
     * @throws IllegalArgumentException
     *      if a project of the give name already exists.
     */
    public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name, boolean notify) throws IOException {
        return itemGroupMixIn.createProject(type, name, notify);
    }

    /**
     * Overwrites the existing item by new one.
     *
     * <p>
     * This is a short cut for deleting an existing job and adding a new one.
     */
    public synchronized void putItem(TopLevelItem item) throws IOException, InterruptedException {
        String name = item.getName();
        TopLevelItem old = items.get(name);
        if (old == item) {
            return; // noop
        }
        checkPermission(Item.CREATE);
        if (old != null) {
            old.delete();
        }
        items.put(name, item);
        ItemListener.fireOnCreated(item);
    }

    /**
     * Creates a new job.
     *
     * <p>
     * This version infers the descriptor from the type of the top-level item.
     *
     * @throws IllegalArgumentException
     *      if the project of the given name already exists.
     */
    public synchronized <T extends TopLevelItem> T createProject(Class<T> type, String name) throws IOException {
        return type.cast(createProject((TopLevelItemDescriptor) getDescriptor(type), name));
    }

    /**
     * Called by {@link Job#renameTo(String)} to update relevant data structure.
     * assumed to be synchronized on Hudson by the caller.
     */
    public void onRenamed(TopLevelItem job, String oldName, String newName) throws IOException {
        items.remove(oldName);
        items.put(newName, job);

        for (View v : views) {
            v.onJobRenamed(job, oldName, newName);
        }
        save();
    }

    /**
     * Called in response to {@link Job#doDoDelete(StaplerRequest, StaplerResponse)}
     */
    public void onDeleted(TopLevelItem item) throws IOException {
        for (ItemListener l : ItemListener.all()) {
            l.onDeleted(item);
        }

        items.remove(item.getName());
        for (View v : views) {
            v.onJobRenamed(item, item.getName(), null);
        }
        save();
    }

    public FingerprintMap getFingerprintMap() {
        return fingerprintMap;
    }

    // if no finger print matches, display "not found page".
    public Object getFingerprint(String md5sum) throws IOException {
        Fingerprint r = fingerprintMap.get(md5sum);
        if (r == null) {
            return new NoFingerprintMatch(md5sum);
        } else {
            return r;
        }
    }

    /**
     * Gets a {@link Fingerprint} object if it exists.
     * Otherwise null.
     */
    public Fingerprint _getFingerprint(String md5sum) throws IOException {
        return fingerprintMap.get(md5sum);
    }

    /**
     * The file we save our configuration.
     */
    private XmlFile getConfigFile() {
        return new XmlFile(XSTREAM, new File(root, "config.xml"));
    }

    public int getNumExecutors() {
        return numExecutors;
    }

    public Mode getMode() {
        return mode;
    }

    public String getLabelString() {
        return fixNull(label).trim();
    }

    @Override
    public LabelAtom getSelfLabel() {
        return getLabelAtom("master");
    }

    public Computer createComputer() {
        return new MasterComputer();
    }

    private synchronized TaskBuilder loadTasks() throws IOException {
        File projectsDir = new File(root, "jobs");
        if (!projectsDir.isDirectory() && !projectsDir.mkdirs()) {
            if (projectsDir.exists()) {
                throw new IOException(projectsDir + " is not a directory");
            }
            throw new IOException("Unable to create " + projectsDir + "\nPermission issue? Please create this directory manually.");
        }
        File[] subdirs = projectsDir.listFiles(new FileFilter() {

            public boolean accept(File child) {
                return child.isDirectory() && Items.getConfigFile(child).exists();
            }
        });

        TaskGraphBuilder g = new TaskGraphBuilder();
        Handle loadHudson = g.requires(EXTENSIONS_AUGMENTED).attains(JOB_LOADED).add("Loading global config", new Executable() {

            public void run(Reactor session) throws Exception {
                XmlFile cfg = getConfigFile();
                if (cfg.exists()) {
                    // reset some data that may not exist in the disk file
                    // so that we can take a proper compensation action later.
                    primaryView = null;
                    views.clear();

                    // load from disk
                    cfg.unmarshal(Hudson.this);
                }

                // if we are loading old data that doesn't have this field
                if (slaves == null) {
                    slaves = new NodeList();
                }

                clouds.setOwner(Hudson.this);
                items.clear();
            }
        });

        for (final File subdir : subdirs) {
            g.requires(loadHudson).attains(JOB_LOADED).notFatal().add("Loading job " + subdir.getName(), new Executable() {

                public void run(Reactor session) throws Exception {
                    TopLevelItem item = (TopLevelItem) Items.load(Hudson.this, subdir);
                    items.put(item.getName(), item);
                }
            });
        }

        g.requires(JOB_LOADED).add("Finalizing set up", new Executable() {

            public void run(Reactor session) throws Exception {
                rebuildDependencyGraph();

                {// recompute label objects - populates the labels mapping.
                    for (Node slave : slaves) // Note that not all labels are visible until the slaves have connected.
                    {
                        slave.getAssignedLabels();
                    }
                    getAssignedLabels();
                }

                // initialize views by inserting the default view if necessary
                // this is both for clean Hudson and for backward compatibility.
                if (views.size() == 0 || primaryView == null) {
                    View v = new AllView(Messages.Hudson_ViewName());
                    v.owner = Hudson.this;
                    views.add(0, v);
                    primaryView = v.getViewName();
                }

                // read in old data that doesn't have the security field set
                if (authorizationStrategy == null) {
                    if (useSecurity == null || !useSecurity) {
                        authorizationStrategy = AuthorizationStrategy.UNSECURED;
                    } else {
                        authorizationStrategy = new LegacyAuthorizationStrategy();
                    }
                }
                if (securityRealm == null) {
                    if (useSecurity == null || !useSecurity) {
                        setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                    } else {
                        setSecurityRealm(new LegacySecurityRealm());
                    }
                } else {
                    // force the set to proxy
                    setSecurityRealm(securityRealm);
                }

                if (useSecurity != null && !useSecurity) {
                    // forced reset to the unsecure mode.
                    // this works as an escape hatch for people who locked themselves out.
                    authorizationStrategy = AuthorizationStrategy.UNSECURED;
                    setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                }

                // Initialize the filter with the crumb issuer
                setCrumbIssuer(crumbIssuer);

                // auto register root actions
                for (Action a : getExtensionList(RootAction.class)) {
                    if (!actions.contains(a)) {
                        actions.add(a);
                    }
                }
            }
        });

        return g;
    }

    /**
     * Save the settings to a file.
     */
    public synchronized void save() throws IOException {
        if (BulkChange.contains(this)) {
            return;
        }
        getConfigFile().write(this);
        SaveableListener.fireOnChange(this, getConfigFile());
    }

    /**
     * Called to shut down the system.
     */
    public void cleanUp() {
        Set<Future<?>> pending = new HashSet<Future<?>>();
        terminating = true;
        for (Computer c : computers.values()) {
            c.interrupt();
            c.kill();
            pending.add(c.disconnect(null));
        }
        if (udpBroadcastThread != null) {
            udpBroadcastThread.shutdown();
        }
        if (dnsMultiCast != null) {
            dnsMultiCast.close();
        }
        ExternalJob.reloadThread.interrupt();
        Trigger.timer.cancel();
        // TODO: how to wait for the completion of the last job?
        Trigger.timer = null;
        if (tcpSlaveAgentListener != null) {
            tcpSlaveAgentListener.shutdown();
        }

        if (pluginManager != null) // be defensive. there could be some ugly timing related issues
        {
            pluginManager.stop();
        }

        if (getRootDir().exists()) // if we are aborting because we failed to create HUDSON_HOME,
        // don't try to save. Issue #536
        {
            getQueue().save();
        }

        threadPoolForLoad.shutdown();
        for (Future<?> f : pending) {
            try {
                f.get(10, TimeUnit.SECONDS);    // if clean up operation didn't complete in time, we fail the test
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;  // someone wants us to die now. quick!
            } catch (ExecutionException e) {
                LOGGER.log(Level.WARNING, "Failed to shut down properly", e);
            } catch (TimeoutException e) {
                LOGGER.log(Level.WARNING, "Failed to shut down properly", e);
            }
        }

        LogFactory.releaseAll();

        theInstance = null;
    }

    public Object getDynamic(String token) {
        for (Action a : getActions()) {
            if (a.getUrlName().equals(token) || a.getUrlName().equals('/' + token)) {
                return a;
            }
        }
        for (Action a : getManagementLinks()) {
            if (a.getUrlName().equals(token)) {
                return a;
            }
        }
        return null;
    }

//
//
// actions
//
//
    /**
     * Accepts submission from the configuration page.
     */
    public synchronized void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        BulkChange bc = new BulkChange(this);
        try {
            checkPermission(ADMINISTER);

            JSONObject json = req.getSubmittedForm();

            // keep using 'useSecurity' field as the main configuration setting
            // until we get the new security implementation working
            // useSecurity = null;
            if (json.has("use_security")) {
                useSecurity = true;
                JSONObject security = json.getJSONObject("use_security");
                setSecurityRealm(SecurityRealm.all().newInstanceFromRadioList(security, "realm"));
                setAuthorizationStrategy(AuthorizationStrategy.all().newInstanceFromRadioList(security, "authorization"));

                if (security.has("markupFormatter")) {
                    markupFormatter = req.bindJSON(MarkupFormatter.class, security.getJSONObject("markupFormatter"));
                } else {
                    markupFormatter = null;
                }
            } else {
                useSecurity = null;
                setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                authorizationStrategy = AuthorizationStrategy.UNSECURED;
                markupFormatter = null;
            }

            if (json.has("csrf")) {
                JSONObject csrf = json.getJSONObject("csrf");
                setCrumbIssuer(CrumbIssuer.all().newInstanceFromRadioList(csrf, "issuer"));
            } else {
                setCrumbIssuer(null);
            }

            if (json.has("viewsTabBar")) {
                viewsTabBar = req.bindJSON(ViewsTabBar.class, json.getJSONObject("viewsTabBar"));
            } else {
                viewsTabBar = new DefaultViewsTabBar();
            }

            if (json.has("myViewsTabBar")) {
                myViewsTabBar = req.bindJSON(MyViewsTabBar.class, json.getJSONObject("myViewsTabBar"));
            } else {
                myViewsTabBar = new DefaultMyViewsTabBar();
            }

            primaryView = json.has("primaryView") ? json.getString("primaryView") : getViews().iterator().next().getViewName();

            noUsageStatistics = json.has("usageStatisticsCollected") ? null : true;

            {
                String v = req.getParameter("slaveAgentPortType");
                if (!isUseSecurity() || v == null || v.equals("random")) {
                    slaveAgentPort = 0;
                } else if (v.equals("disable")) {
                    slaveAgentPort = -1;
                } else {
                    try {
                        slaveAgentPort = Integer.parseInt(req.getParameter("slaveAgentPort"));
                    } catch (NumberFormatException e) {
                        throw new FormException(Messages.Hudson_BadPortNumber(req.getParameter("slaveAgentPort")), "slaveAgentPort");
                    }
                }

                // relaunch the agent
                if (tcpSlaveAgentListener == null) {
                    if (slaveAgentPort != -1) {
                        tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
                    }
                } else {
                    if (tcpSlaveAgentListener.configuredPort != slaveAgentPort) {
                        tcpSlaveAgentListener.shutdown();
                        tcpSlaveAgentListener = null;
                        if (slaveAgentPort != -1) {
                            tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
                        }
                    }
                }
            }

            numExecutors = json.getInt("numExecutors");
            if (req.hasParameter("master.mode")) {
                mode = Mode.valueOf(req.getParameter("master.mode"));
            } else {
                mode = Mode.NORMAL;
            }

            label = json.optString("labelString", "");

            quietPeriod = json.getInt("quiet_period");

            scmCheckoutRetryCount = json.getInt("retry_count");

            systemMessage = Util.nullify(req.getParameter("system_message"));

            jdks.clear();
            jdks.addAll(req.bindJSONToList(JDK.class, json.get("jdks")));

            boolean result = true;
            for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfig()) {
                result &= configureDescriptor(req, json, d);
            }

            for (JSONObject o : StructuredForm.toList(json, "plugin")) {
                pluginManager.getPlugin(o.getString("name")).getPlugin().configure(req, o);
            }

            clouds.rebuildHetero(req, json, Cloud.all(), "cloud");

            JSONObject np = json.getJSONObject("globalNodeProperties");
            if (np != null) {
                globalNodeProperties.rebuild(req, np, NodeProperty.for_(this));
            }

            version = VERSION;

            save();
            updateComputerList();
            if (result) {
                rsp.sendRedirect(req.getContextPath() + '/');  // go to the top page
            } else {
                rsp.sendRedirect("configure"); // back to config
            }
        } finally {
            bc.commit();
        }
    }

    public CrumbIssuer getCrumbIssuer() {
        return crumbIssuer;
    }

    public void setCrumbIssuer(CrumbIssuer issuer) {
        crumbIssuer = issuer;
    }

    public synchronized void doTestPost(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.sendRedirect("foo");
    }

    private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d) throws FormException {
        // collapse the structure to remain backward compatible with the JSON structure before 1.
        String name = d.getJsonSafeClassName();
        JSONObject js = json.has(name) ? json.getJSONObject(name) : new JSONObject(); // if it doesn't have the property, the method returns invalid null object.
        json.putAll(js);
        return d.configure(req, js);
    }

    /**
     * Accepts submission from the configuration page.
     */
    public synchronized void doConfigExecutorsSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(ADMINISTER);

        BulkChange bc = new BulkChange(this);
        try {
            JSONObject json = req.getSubmittedForm();

            setNumExecutors(Integer.parseInt(req.getParameter("numExecutors")));
            if (req.hasParameter("master.mode")) {
                mode = Mode.valueOf(req.getParameter("master.mode"));
            } else {
                mode = Mode.NORMAL;
            }

            setNodes(req.bindJSONToList(Slave.class, json.get("slaves")));
        } finally {
            bc.commit();
        }

        rsp.sendRedirect(req.getContextPath() + '/');  // go to the top page
    }

    /**
     * Accepts the new description.
     */
    public synchronized void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        getPrimaryView().doSubmitDescription(req, rsp);
    }

    /**
     * @deprecated as of 1.317
     *      Use {@link #doQuietDown()} instead.
     */
    public synchronized void doQuietDown(StaplerResponse rsp) throws IOException, ServletException {
        doQuietDown().generateResponse(null, rsp, this);
    }

    public synchronized HttpRedirect doQuietDown() throws IOException {
        try {
            return doQuietDown(false, 0);
        } catch (InterruptedException e) {
            throw new AssertionError(); // impossible
        }
    }

    @CLIMethod(name = "quiet-down")
    public HttpRedirect doQuietDown(
            @Option(name = "-block", usage = "Block until the system really quiets down and no builds are running") @QueryParameter boolean block,
            @Option(name = "-timeout", usage = "If non-zero, only block up to the specified number of milliseconds") @QueryParameter int timeout) throws InterruptedException, IOException {
        synchronized (this) {
            checkPermission(ADMINISTER);
            isQuietingDown = true;
        }
        if (block) {
            if (timeout > 0) {
                timeout += System.currentTimeMillis();
            }
            while (isQuietingDown
                    && (timeout <= 0 || System.currentTimeMillis() < timeout)
                    && !RestartListener.isAllReady()) {
                Thread.sleep(1000);
            }
        }
        return new HttpRedirect(".");
    }

    @CLIMethod(name = "cancel-quiet-down")
    public synchronized HttpRedirect doCancelQuietDown() {
        checkPermission(ADMINISTER);
        isQuietingDown = false;
        getQueue().scheduleMaintenance();
        return new HttpRedirect(".");
    }

    /**
     * Backward compatibility. Redirect to the thread dump.
     */
    public void doClassicThreadDump(StaplerResponse rsp) throws IOException, ServletException {
        rsp.sendRedirect2("threadDump");
    }

    public synchronized Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return itemGroupMixIn.createTopLevelItem(req, rsp);
    }

    /**
     * Creates a new job from its configuration XML. The type of the job created will be determined by
     * what's in this XML.
     * @since 1.319
     */
    public TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
        return itemGroupMixIn.createProjectFromXML(name, xml);
    }

    /**
     * Reload a project to update its definition.
     * @since 2.x.x
     */
    public TopLevelItem reloadProjectFromDisk(File jobDir) throws IOException {
        TopLevelItem item = (TopLevelItem) Items.load(this, jobDir);
        items.put(item.getName(), item);
        rebuildDependencyGraph();
        return item;
    }

    /**
     * Copys a job.
     *
     * @param src
     *      A {@link TopLevelItem} to be copied.
     * @param name
     *      Name of the newly created project.
     * @return
     *      Newly created {@link TopLevelItem}.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends TopLevelItem> T copy(T src, String name) throws IOException {
        return itemGroupMixIn.copy(src, name);
    }

    // a little more convenient overloading that assumes the caller gives us the right type
    // (or else it will fail with ClassCastException)
    public <T extends AbstractProject<?, ?>> T copy(T src, String name) throws IOException {
        return (T) copy((TopLevelItem) src, name);
    }

    public synchronized void doCreateView(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        checkPermission(View.CREATE);
        addView(View.create(req, rsp, this));
    }

    /**
     * Check if the given name is suitable as a name
     * for job, view, etc.
     *
     * @throws ParseException
     *      if the given name is not good
     */
    public static void checkGoodName(String name) throws Failure {
        if (name == null || name.length() == 0) {
            throw new Failure(Messages.Hudson_NoName());
        }

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isISOControl(ch)) {
                throw new Failure(Messages.Hudson_ControlCodeNotAllowed(toPrintableName(name)));
            }
            if ("?*/\\%!@#$^&|<>[]:;".indexOf(ch) != -1) {
                throw new Failure(Messages.Hudson_UnsafeChar(ch));
            }
        }

        // looks good
    }

    /**
     * Makes sure that the given name is good as a job name.
     * @return trimmed name if valid; throws ParseException if not
     */
    private String checkJobName(String name) throws Failure {
        checkGoodName(name);
        name = name.trim();
        if (getItem(name) != null) {
            throw new Failure(Messages.Hudson_JobAlreadyExists(name));
        }
        // looks good
        return name;
    }

    private static String toPrintableName(String name) {
        StringBuilder printableName = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isISOControl(ch)) {
                printableName.append("\\u").append((int) ch).append(';');
            } else {
                printableName.append(ch);
            }
        }
        return printableName.toString();
    }

    /**
     * Checks if the user was successfully authenticated.
     *
     * @see BasicAuthenticationFilter
     */
    public void doSecured(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if (req.getUserPrincipal() == null) {
            // authentication must have failed
            rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // the user is now authenticated, so send him back to the target
        String path = req.getContextPath() + req.getOriginalRestOfPath();
        String q = req.getQueryString();
        if (q != null) {
            path += '?' + q;
        }

        rsp.sendRedirect2(path);
    }

    /**
     * Called once the user logs in. Just forward to the top page.
     */
    public void doLoginEntry(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (req.getUserPrincipal() == null) {
            rsp.sendRedirect2("noPrincipal");
            return;
        }

        String from = req.getParameter("from");
        if (from != null && from.startsWith("/") && !from.equals("/loginError")) {
            rsp.sendRedirect2(from);    // I'm bit uncomfortable letting users redircted to other sites, make sure the URL falls into this domain
            return;
        }

        String url = AbstractProcessingFilter.obtainFullRequestUrl(req);
        if (url != null) {
            // if the login redirect is initiated by Acegi
            // this should send the user back to where s/he was from.
            rsp.sendRedirect2(url);
            return;
        }

        rsp.sendRedirect2(".");
    }

    /**
     * Logs out the user.
     */
    public void doLogout(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        securityRealm.doLogout(req, rsp);
    }

    /**
     * Serves jar files for JNLP slave agents.
     */
    public Slave.JnlpJar getJnlpJars(String fileName) {
        return new Slave.JnlpJar(fileName);
    }

    public Slave.JnlpJar doJnlpJars(StaplerRequest req) {
        return new Slave.JnlpJar(req.getRestOfPath());
    }

    /**
     * RSS feed for log entries.
     *
     * @deprecated
     *   As on 1.267, moved to "/log/rss..."
     */
    public void doLogRss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String qs = req.getQueryString();
        rsp.sendRedirect2("./log/rss" + (qs == null ? "" : '?' + qs));
    }

    /**
     * Reloads the configuration.
     */
    @CLIMethod(name = "reload-configuration")
    public synchronized HttpResponse doReload() throws IOException {
        checkPermission(ADMINISTER);

        // engage "loading ..." UI and then run the actual task in a separate thread
        WebAppController.get().install( new HudsonIsLoading());

        new Thread("Hudson config reload thread") {

            @Override
            public void run() {
                try {
                    SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
                    reload();
                } catch (IOException e) {
                    LOGGER.log(SEVERE, "Failed to reload Hudson config", e);
                } catch (ReactorException e) {
                    LOGGER.log(SEVERE, "Failed to reload Hudson config", e);
                } catch (InterruptedException e) {
                    LOGGER.log(SEVERE, "Failed to reload Hudson config", e);
                }
            }
        }.start();

        return HttpResponses.redirectViaContextPath("/");
    }

    /**
     * Reloads the configuration synchronously.
     */
    public void reload() throws IOException, InterruptedException, ReactorException {
        executeReactor(null, loadTasks());
        User.reload();
        initLevel = InitMilestone.COMPLETED;
        WebAppController.get().install( this);
    }

    /**
     * Do a finger-print check.
     */
    public void doDoFingerprintCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        // Parse the request
        MultipartFormDataParser p = new MultipartFormDataParser(req);
        if (Hudson.getInstance().isUseCrumbs() && !Hudson.getInstance().getCrumbIssuer().validateCrumb(req, p)) {
            rsp.sendError(HttpServletResponse.SC_FORBIDDEN, "No crumb found");
        }
        try {
            rsp.sendRedirect2(req.getContextPath() + "/fingerprint/"
                    + Util.getDigestOf(p.getFileItem("name").getInputStream()) + '/');
        } finally {
            p.cleanUp();
        }
    }

    /**
     * For debugging. Expose URL to perform GC.
     */
    public void doGc(StaplerResponse rsp) throws IOException {
        checkPermission(Hudson.ADMINISTER);
        System.gc();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("GCed");
    }

    /**
     * Obtains the heap dump.
     */
    public HeapDump getHeapDump() throws IOException {
        return new HeapDump(this, MasterComputer.localChannel);
    }

    /**
     * Simulates OutOfMemoryError.
     * Useful to make sure OutOfMemoryHeapDump setting.
     */
    public void doSimulateOutOfMemory() throws IOException {
        checkPermission(ADMINISTER);

        System.out.println("Creating artificial OutOfMemoryError situation");
        List<Object> args = new ArrayList<Object>();
        while (true) {
            args.add(new byte[1024 * 1024]);
        }
    }
    private transient final Map<UUID, FullDuplexHttpChannel> duplexChannels = new HashMap<UUID, FullDuplexHttpChannel>();

    /**
     * Handles HTTP requests for duplex channels for CLI.
     */
    public void doCli(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        if (!"POST".equals(req.getMethod())) {
            // for GET request, serve _cli.jelly, assuming this is a browser
            checkPermission(READ);
            req.getView(this, "_cli.jelly").forward(req, rsp);
            return;
        }

        // do not require any permission to establish a CLI connection
        // the actual authentication for the connecting Channel is done by CLICommand

        UUID uuid = UUID.fromString(req.getHeader("Session"));
        rsp.setHeader("Hudson-Duplex", ""); // set the header so that the client would know

        FullDuplexHttpChannel server;
        if (req.getHeader("Side").equals("download")) {
            duplexChannels.put(uuid, server = new FullDuplexHttpChannel(uuid, !hasPermission(ADMINISTER)) {

                protected void main(Channel channel) throws IOException, InterruptedException {
                    // capture the identity given by the transport, since this can be useful for SecurityRealm.createCliAuthenticator()
                    channel.setProperty(CLICommand.TRANSPORT_AUTHENTICATION, getAuthentication());
                    channel.setProperty(CliEntryPoint.class.getName(), new CliManagerImpl());
                }
            });
            try {
                server.download(req, rsp);
            } finally {
                duplexChannels.remove(uuid);
            }
        } else {
            duplexChannels.get(uuid).upload(req, rsp);
        }
    }

    /**
     * Binds /userContent/... to $HUDSON_HOME/userContent.
     */
    public DirectoryBrowserSupport doUserContent() {
        return new DirectoryBrowserSupport(this, getRootPath().child("userContent"), "User content", "folder.gif", true);
    }

    /**
     * Perform a restart of Hudson, if we can.
     *
     * This first replaces "app" to {@link HudsonIsRestarting}
     */
    @CLIMethod(name = "restart")
    public void doRestart(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, RestartNotSupportedException {
        checkPermission(ADMINISTER);
        if (req != null && req.getMethod().equals("GET")) {
            req.getView(this, "_restart.jelly").forward(req, rsp);
            return;
        }

        restart();

        if (rsp != null) // null for CLI
        {
            rsp.sendRedirect2(".");
        }
    }

    /**
     * Queues up a restart of Hudson for when there are no builds running, if we can.
     *
     * This first replaces "app" to {@link HudsonIsRestarting}
     *
     * @since 1.332
     */
    @CLIMethod(name = "safe-restart")
    public void doSafeRestart(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, RestartNotSupportedException {
        checkPermission(ADMINISTER);
        if (req != null && req.getMethod().equals("GET")) {
            req.getView(this, "_safeRestart.jelly").forward(req, rsp);
            return;
        }

        safeRestart();

        if (rsp != null) // null for CLI
        {
            rsp.sendRedirect2(".");
        }
    }

    /**
     * Performs a restart.
     */
    public void restart() throws RestartNotSupportedException {
        final Lifecycle lifecycle = Lifecycle.get();
        lifecycle.verifyRestartable(); // verify that Hudson is restartable
        WebAppController.get().install( new HudsonIsRestarting());

        new Thread("restart thread") {

            final String exitUser = getAuthentication().getName();

            @Override
            public void run() {
                try {
                    SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);

                    // give some time for the browser to load the "reloading" page
                    Thread.sleep(5000);
                    LOGGER.severe(String.format("Restarting VM as requested by %s", exitUser));
                    for (RestartListener listener : RestartListener.all()) {
                        listener.onRestart();
                    }
                    lifecycle.restart();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Hudson", e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Hudson", e);
                }
            }
        }.start();
    }

    /**
     * Queues up a restart to be performed once there are no builds currently running.
     * @since 1.332
     */
    public void safeRestart() throws RestartNotSupportedException {
        final Lifecycle lifecycle = Lifecycle.get();
        lifecycle.verifyRestartable(); // verify that Hudson is restartable
        // Quiet down so that we won't launch new builds.
        isQuietingDown = true;

        new Thread("safe-restart thread") {

            final String exitUser = getAuthentication().getName();

            @Override
            public void run() {
                try {
                    SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);

                    // Wait 'til we have no active executors.
                    doQuietDown(true, 0);

                    // Make sure isQuietingDown is still true.
                    if (isQuietingDown) {
                        servletContext.setAttribute("app", new HudsonIsRestarting());
                        // give some time for the browser to load the "reloading" page
                        LOGGER.info("Restart in 10 seconds");
                        Thread.sleep(10000);
                        LOGGER.severe(String.format("Restarting VM as requested by %s", exitUser));
                        for (RestartListener listener : RestartListener.all()) {
                            listener.onRestart();
                        }
                        lifecycle.restart();
                    } else {
                        LOGGER.info("Safe-restart mode cancelled");
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Hudson", e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Hudson", e);
                }
            }
        }.start();
    }

    /**
     * Shutdown the system.
     * @since 1.161
     */
    public void doExit(StaplerRequest req, StaplerResponse rsp) throws IOException {
        checkPermission(ADMINISTER);
        LOGGER.severe(String.format("Shutting down VM as requested by %s from %s",
                getAuthentication().getName(), req.getRemoteAddr()));
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        PrintWriter w = rsp.getWriter();
        w.println("Shutting down");
        w.close();

        System.exit(0);
    }

    /**
     * Shutdown the system safely.
     * @since 1.332
     */
    public void doSafeExit(StaplerRequest req, StaplerResponse rsp) throws IOException {
        checkPermission(ADMINISTER);
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        PrintWriter w = rsp.getWriter();
        w.println("Shutting down as soon as all jobs are complete");
        w.close();
        isQuietingDown = true;
        final String exitUser = getAuthentication().getName();
        final String exitAddr = req.getRemoteAddr().toString();
        new Thread("safe-exit thread") {

            @Override
            public void run() {
                try {
                    SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
                    LOGGER.severe(String.format("Shutting down VM as requested by %s from %s",
                            exitUser, exitAddr));
                    // Wait 'til we have no active executors.
                    while (isQuietingDown
                            && (overallLoad.computeTotalExecutors() > overallLoad.computeIdleExecutors())) {
                        Thread.sleep(5000);
                    }
                    // Make sure isQuietingDown is still true.
                    if (isQuietingDown) {
                        cleanUp();
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to shutdown Hudson", e);
                }
            }
        }.start();
    }

    /**
     * Gets the {@link Authentication} object that represents the user
     * associated with the current request.
     */
    public static Authentication getAuthentication() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        // on Tomcat while serving the login page, this is null despite the fact
        // that we have filters. Looking at the stack trace, Tomcat doesn't seem to
        // run the request through filters when this is the login request.
        // see http://www.nabble.com/Matrix-authorization-problem-tp14602081p14886312.html
        if (a == null) {
            a = ANONYMOUS;
        }
        return a;
    }

    /**
     * For system diagnostics.
     * Run arbitrary Groovy script.
     */
    public void doScript(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        doScript(req, rsp, req.getView(this, "_script.jelly"));
    }

    /**
     * Run arbitrary Groovy script and return result as plain text.
     */
    public void doScriptText(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        doScript(req, rsp, req.getView(this, "_scriptText.jelly"));
    }

    private void doScript(StaplerRequest req, StaplerResponse rsp, RequestDispatcher view) throws IOException, ServletException {
        // ability to run arbitrary script is dangerous
        checkPermission(ADMINISTER);

        String text = req.getParameter("script");
        if (text != null) {
            try {
                req.setAttribute("output",
                        RemotingDiagnostics.executeGroovy(text, MasterComputer.localChannel));
            } catch (InterruptedException e) {
                throw new ServletException(e);
            }
        }

        view.forward(req, rsp);
    }

    /**
     * Evaluates the Jelly script submitted by the client.
     *
     * This is useful for system administration as well as unit testing.
     */
    public void doEval(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(ADMINISTER);
        requirePOST();

        try {
            MetaClass mc = WebApp.getCurrent().getMetaClass(getClass());
            Script script = mc.classLoader.loadTearOff(JellyClassLoaderTearOff.class).createContext().compileScript(new InputSource(req.getReader()));
            new JellyRequestDispatcher(this, script).forward(req, rsp);
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Sign up for the user account.
     */
    public void doSignup(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        req.getView(getSecurityRealm(), "signup.jelly").forward(req, rsp);
    }

    /**
     * Changes the icon size by changing the cookie
     */
    public void doIconSize(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String qs = req.getQueryString();
        if (qs == null || !ICON_SIZE.matcher(qs).matches()) {
            throw new ServletException();
        }
        Cookie cookie = new Cookie("iconSize", qs);
        cookie.setMaxAge(/* ~4 mo. */9999999); // #762
        rsp.addCookie(cookie);
        String ref = req.getHeader("Referer");
        if (ref == null) {
            ref = ".";
        }
        rsp.sendRedirect2(ref);
    }

    public void doFingerprintCleanup(StaplerResponse rsp) throws IOException {
        FingerprintCleanupThread.invoke();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("Invoked");
    }

    public void doWorkspaceCleanup(StaplerResponse rsp) throws IOException {
        WorkspaceCleanupThread.invoke();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("Invoked");
    }

    /**
     * If the user chose the default JDK, make sure we got 'java' in PATH.
     */
    public FormValidation doDefaultJDKCheck(StaplerRequest request, @QueryParameter String value) {
        if (!value.equals("(Default)")) // assume the user configured named ones properly in system config ---
        // or else system config should have reported form field validation errors.
        {
            return FormValidation.ok();
        }

        // default JDK selected. Does such java really exist?
        if (JDK.isDefaultJDKValid(Hudson.this)) {
            return FormValidation.ok();
        } else {
            return FormValidation.errorWithMarkup(Messages.Hudson_NoJavaInPath(request.getContextPath()));
        }
    }

    /**
     * Makes sure that the given name is good as a job name.
     */
    public FormValidation doCheckJobName(@QueryParameter String value) {
        // this method can be used to check if a file exists anywhere in the file system,
        // so it should be protected.
        checkPermission(Item.CREATE);

        if (fixEmpty(value) == null) {
            return FormValidation.ok();
        }

        try {
            checkJobName(value);
            return FormValidation.ok();
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }
    }

    /**
     * Checks if a top-level view with the given name exists.
     */
    public FormValidation doViewExistsCheck(@QueryParameter String value) {
        checkPermission(View.CREATE);

        String view = fixEmpty(value);
        if (view == null) {
            return FormValidation.ok();
        }

        if (getView(view) == null) {
            return FormValidation.ok();
        } else {
            return FormValidation.error(Messages.Hudson_ViewAlreadyExists(view));
        }
    }

    /**
     * @deprecated as of 1.294
     *      Define your own check method, instead of relying on this generic one.
     */
    public void doFieldCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        doFieldCheck(
                fixEmpty(req.getParameter("value")),
                fixEmpty(req.getParameter("type")),
                fixEmpty(req.getParameter("errorText")),
                fixEmpty(req.getParameter("warningText"))).generateResponse(req, rsp, this);
    }

    /**
     * Checks if the value for a field is set; if not an error or warning text is displayed.
     * If the parameter "value" is not set then the parameter "errorText" is displayed
     * as an error text. If the parameter "errorText" is not set, then the parameter "warningText"
     * is displayed as a warning text.
     * <p>
     * If the text is set and the parameter "type" is set, it will validate that the value is of the
     * correct type. Supported types are "number, "number-positive" and "number-negative".
     *
     * @deprecated as of 1.324
     *      Either use client-side validation (e.g. class="required number")
     *      or define your own check method, instead of relying on this generic one.
     */
    public FormValidation doFieldCheck(@QueryParameter(fixEmpty = true) String value,
            @QueryParameter(fixEmpty = true) String type,
            @QueryParameter(fixEmpty = true) String errorText,
            @QueryParameter(fixEmpty = true) String warningText) {
        if (value == null) {
            if (errorText != null) {
                return FormValidation.error(errorText);
            }
            if (warningText != null) {
                return FormValidation.warning(warningText);
            }
            return FormValidation.error("No error or warning text was set for fieldCheck().");
        }

        if (type != null) {
            try {
                if (type.equalsIgnoreCase("number")) {
                    NumberFormat.getInstance().parse(value);
                } else if (type.equalsIgnoreCase("number-positive")) {
                    if (NumberFormat.getInstance().parse(value).floatValue() <= 0) {
                        return FormValidation.error(Messages.Hudson_NotAPositiveNumber());
                    }
                } else if (type.equalsIgnoreCase("number-negative")) {
                    if (NumberFormat.getInstance().parse(value).floatValue() >= 0) {
                        return FormValidation.error(Messages.Hudson_NotANegativeNumber());
                    }
                }
            } catch (ParseException e) {
                return FormValidation.error(Messages.Hudson_NotANumber());
            }
        }

        return FormValidation.ok();
    }

    /**
     * Serves static resources placed along with Jelly view files.
     * <p>
     * This method can serve a lot of files, so care needs to be taken
     * to make this method secure. It's not clear to me what's the best
     * strategy here, though the current implementation is based on
     * file extensions.
     */
    public void doResources(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        // cut off the "..." portion of /resources/.../path/to/file
        // as this is only used to make path unique (which in turn
        // allows us to set a long expiration date
        path = path.substring(path.indexOf('/', 1) + 1);

        int idx = path.lastIndexOf('.');
        String extension = path.substring(idx + 1);
        if (ALLOWED_RESOURCE_EXTENSIONS.contains(extension)) {
            URL url = pluginManager.uberClassLoader.getResource(path);
            if (url != null) {
                long expires = MetaClass.NO_CACHE ? 0 : 365L * 24 * 60 * 60 * 1000; /*1 year*/
                rsp.serveFile(req, url, expires);
                return;
            }
        }
        rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    /**
     * Extension list that {@link #doResources(StaplerRequest, StaplerResponse)} can serve.
     * This set is mutable to allow plugins to add additional extensions.
     */
    public static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = new HashSet<String>(Arrays.asList(
            "js|css|jpeg|jpg|png|gif|html|htm".split("\\|")));

    /**
     * Checks if container uses UTF-8 to decode URLs. See
     * http://wiki.hudson-ci.com/display/HUDSON/Tomcat#Tomcat-i18n
     */
    public FormValidation doCheckURIEncoding(StaplerRequest request) throws IOException {
        // expected is non-ASCII String
        final String expected = "\u57f7\u4e8b";
        final String value = fixEmpty(request.getParameter("value"));
        if (!expected.equals(value)) {
            return FormValidation.warningWithMarkup(Messages.Hudson_NotUsesUTF8ToDecodeURL());
        }
        return FormValidation.ok();
    }

    /**
     * Does not check when system default encoding is "ISO-8859-1".
     */
    public static boolean isCheckURIEncodingEnabled() {
        return !"ISO-8859-1".equalsIgnoreCase(System.getProperty("file.encoding"));
    }

    /**
     * @deprecated
     *      Use {@link Functions#isWindows()}.
     */
    public static boolean isWindows() {
        return File.pathSeparatorChar == ';';
    }

    public static boolean isDarwin() {
        // according to http://developer.apple.com/technotes/tn2002/tn2110.html
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("mac");
    }

    /**
     * Rebuilds the dependency map.
     */
    public void rebuildDependencyGraph() {
        dependencyGraph = new DependencyGraph();
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    // for Jelly
    public List<ManagementLink> getManagementLinks() {
        return ManagementLink.all();
    }

    /**
     * Exposes the current user to <tt>/me</tt> URL.
     */
    public User getMe() {
        User u = User.current();
        if (u == null) {
            throw new AccessDeniedException("/me is not available when not logged in");
        }
        return u;
    }

    /**
     * Gets the {@link Widget}s registered on this object.
     *
     * <p>
     * Plugins who wish to contribute boxes on the side panel can add widgets
     * by {@code getWidgets().add(new MyWidget())} from {@link Plugin#start()}.
     */
    public List<Widget> getWidgets() {
        return widgets;
    }

    public Object getTarget() {
        try {
            checkPermission(READ);
        } catch (AccessDeniedException e) {
            String rest = Stapler.getCurrentRequest().getRestOfPath();
            if (rest.startsWith("/login")
                    || rest.startsWith("/logout")
                    || rest.startsWith("/accessDenied")
                    || rest.startsWith("/signup")
                    || rest.startsWith("/jnlpJars/")
                    || rest.startsWith("/tcpSlaveAgentListener")
                    || rest.startsWith("/cli")
                    || rest.startsWith("/whoAmI")
                    || rest.startsWith("/federatedLoginService/")
                    || rest.startsWith("/securityRealm")) {
                return this;    // URLs that are always visible without READ permission
            }
            throw e;
        }
        return this;
    }
    
    public Iterator<GlobalMessage> getGlobalMessages() {
        return Iterators.readOnly(getExtensionList(GlobalMessage.class).iterator());
    }

    /**
     * Fallback to the primary view.
     */
    public View getStaplerFallback() {
        return getPrimaryView();
    }

    public static final class MasterComputer extends Computer {

        private MasterComputer() {
            super(Hudson.getInstance());
        }

        /**
         * Returns "" to match with {@link Hudson#getNodeName()}.
         */
        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean isConnecting() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.Hudson_Computer_DisplayName();
        }

        @Override
        public String getCaption() {
            return Messages.Hudson_Computer_Caption();
        }

        @Override
        public String getUrl() {
            return "computer/(master)/";
        }

        public RetentionStrategy getRetentionStrategy() {
            return RetentionStrategy.NOOP;
        }

        /**
         * Report an error.
         */
        @Override
        public HttpResponse doDoDelete() throws IOException {
            throw HttpResponses.status(SC_BAD_REQUEST);
        }

        @Override
        public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            // the master node isn't in the Hudson.getNodes(), so this method makes no sense.
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPermission(Permission permission) {
            // no one should be allowed to delete the master.
            // this hides the "delete" link from the /computer/(master) page.
            if (permission == Computer.DELETE) {
                return false;
            }
            // Configuration of master node requires ADMINISTER permission
            return super.hasPermission(permission == Computer.CONFIGURE ? Hudson.ADMINISTER : permission);
        }

        @Override
        public VirtualChannel getChannel() {
            return localChannel;
        }

        @Override
        public Charset getDefaultCharset() {
            return Charset.defaultCharset();
        }

        public List<LogRecord> getLogRecords() throws IOException, InterruptedException {
            return logRecords;
        }

        public void doLaunchSlaveAgent(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            // this computer never returns null from channel, so
            // this method shall never be invoked.
            rsp.sendError(SC_NOT_FOUND);
        }

        /**
         * Redirect the master configuration to /configure.
         */
        public void doConfigure(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            rsp.sendRedirect2(req.getContextPath() + "/configure");
        }

        protected Future<?> _connect(boolean forceReconnect) {
            return Futures.precomputed(null);
        }
        /**
         * {@link LocalChannel} instance that can be used to execute programs locally.
         */
        public static final LocalChannel localChannel = new LocalChannel(threadPoolForRemoting);
    }

    /**
     * Shortcut for {@code Hudson.getInstance().lookup.get(type)}
     */
    public static <T> T lookup(Class<T> type) {
        return Hudson.getInstance().lookup.get(type);
    }

    /**
     * @deprecated since 2007-12-18.
     *      Use {@link #checkPermission(Permission)}
     */
    public static boolean adminCheck() throws IOException {
        return adminCheck(Stapler.getCurrentRequest(), Stapler.getCurrentResponse());
    }

    /**
     * @deprecated since 2007-12-18.
     *      Use {@link #checkPermission(Permission)}
     */
    public static boolean adminCheck(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (isAdmin(req)) {
            return true;
        }

        rsp.sendError(StaplerResponse.SC_FORBIDDEN);
        return false;
    }

    /**
     * Checks if the current user (for which we are processing the current request)
     * has the admin access.
     *
     * @deprecated since 2007-12-18.
     *      This method is deprecated when Hudson moved from simple Unix root-like model
     *      of "admin gets to do everything, and others don't have any privilege" to more
     *      complex {@link ACL} and {@link Permission} based scheme.
     *
     *      <p>
     *      For a quick migration, use {@code Hudson.getInstance().getACL().hasPermission(Hudson.ADMINISTER)}
     *      To check if the user has the 'administer' role in Hudson.
     *
     *      <p>
     *      But ideally, your plugin should first identify a suitable {@link Permission} (or create one,
     *      if appropriate), then identify a suitable {@link AccessControlled} object to check its permission
     *      against.
     */
    public static boolean isAdmin() {
        return Hudson.getInstance().getACL().hasPermission(ADMINISTER);
    }

    /**
     * @deprecated since 2007-12-18.
     *      Define a custom {@link Permission} and check against ACL.
     *      See {@link #isAdmin()} for more instructions.
     */
    public static boolean isAdmin(StaplerRequest req) {
        return isAdmin();
    }
    /**
     * Live view of recent {@link LogRecord}s produced by Hudson.
     */
    public static List<LogRecord> logRecords = Collections.emptyList(); // initialized to dummy value to avoid NPE
    /**
     * Thread-safe reusable {@link XStream}.
     */
    public static final XStream XSTREAM = new XStream2();
    private static final int TWICE_CPU_NUM = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * Thread pool used to load configuration in parallel, to improve the start up time.
     * <p>
     * The idea here is to overlap the CPU and I/O, so we want more threads than CPU numbers.
     */
    /*package*/ transient final ExecutorService threadPoolForLoad = new ThreadPoolExecutor(
            TWICE_CPU_NUM, TWICE_CPU_NUM,
            5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory());

    private static void computeVersion(ServletContext context) {
        // set the version
        Properties props = new Properties();
        try {
            InputStream is = Hudson.class.getResourceAsStream("hudson-version.properties");
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace(); // if the version properties is missing, that's OK.
        }
        String ver = props.getProperty("version");
        if (ver == null) {
            ver = "?";
        }
        VERSION = ver;
        context.setAttribute("version", ver);
        VERSION_HASH = Util.getDigestOf(ver).substring(0, 8);

        if (ver.equals("?") || Boolean.getBoolean("hudson.script.noCache")) {
            RESOURCE_PATH = "";
        } else {
            RESOURCE_PATH = "/static/" + VERSION_HASH;
        }

        VIEW_RESOURCE_PATH = "/resources/" + VERSION_HASH;
    }
    /**
     * Version number of this Hudson.
     */
    public static String VERSION = "?";

    /**
     * Parses {@link #VERSION} into {@link VersionNumber}, or null if it's not parseable as a version number
     * (such as when Hudson is run with "mvn hudson-dev:run")
     */
    public static VersionNumber getVersion() {
        try {
            return new VersionNumber(VERSION);
        } catch (NumberFormatException e) {
            try {
                // for non-released version of Hudson, this looks like "1.345 (private-foobar), so try to approximate.
                int idx = VERSION.indexOf(' ');
                if (idx > 0) {
                    return new VersionNumber(VERSION.substring(0, idx));
                }
            } catch (NumberFormatException _) {
                // fall through
            }

            // totally unparseable
            return null;
        } catch (IllegalArgumentException e) {
            // totally unparseable
            return null;
        }
    }
    /**
     * Hash of {@link #VERSION}.
     */
    public static String VERSION_HASH;
    /**
     * Prefix to static resources like images and javascripts in the war file.
     * Either "" or strings like "/static/VERSION", which avoids Hudson to pick up
     * stale cache when the user upgrades to a different version.
     * <p>
     * Value computed in {@link WebAppMain}.
     */
    public static String RESOURCE_PATH = "";
    /**
     * Prefix to resources alongside view scripts.
     * Strings like "/resources/VERSION", which avoids Hudson to pick up
     * stale cache when the user upgrades to a different version.
     * <p>
     * Value computed in {@link WebAppMain}.
     */
    public static String VIEW_RESOURCE_PATH = "/resources/TBD";
    public static boolean PARALLEL_LOAD = !"false".equals(System.getProperty(Hudson.class.getName() + ".parallelLoad"));
    public static boolean KILL_AFTER_LOAD = Boolean.getBoolean(Hudson.class.getName() + ".killAfterLoad");
    public static boolean LOG_STARTUP_PERFORMANCE = Boolean.getBoolean(Hudson.class.getName() + ".logStartupPerformance");
    private static final boolean CONSISTENT_HASH = true; // Boolean.getBoolean(Hudson.class.getName()+".consistentHash");
    /**
     * Enabled by default as of 1.337. Will keep it for a while just in case we have some serious problems.
     */
    public static boolean FLYWEIGHT_SUPPORT = !"false".equals(System.getProperty(Hudson.class.getName() + ".flyweightSupport"));
    /**
     * Tentative switch to activate the concurrent build behavior.
     * When we merge this back to the trunk, this allows us to keep
     * this feature hidden for a while until we iron out the kinks.
     * @see AbstractProject#isConcurrentBuild()
     */
    public static boolean CONCURRENT_BUILD = true;
    /**
     * Switch to enable people to use a shorter workspace name.
     */
    private static final String WORKSPACE_DIRNAME = System.getProperty(Hudson.class.getName() + ".workspaceDirName", "workspace");
    /**
     * Automatically try to launch a slave when Hudson is initialized or a new slave is created.
     */
    public static boolean AUTOMATIC_SLAVE_LAUNCH = true;
    private static final Logger LOGGER = Logger.getLogger(Hudson.class.getName());
    private static final Pattern ICON_SIZE = Pattern.compile("\\d+x\\d+");
    public static final PermissionGroup PERMISSIONS = Permission.HUDSON_PERMISSIONS;
    public static final Permission ADMINISTER = Permission.HUDSON_ADMINISTER;
    public static final Permission READ = new Permission(PERMISSIONS, "Read", Messages._Hudson_ReadPermission_Description(), Permission.READ);
    /**
     * {@link Authentication} object that represents the anonymous user.
     * Because Acegi creates its own {@link AnonymousAuthenticationToken} instances, the code must not
     * expect the singleton semantics. This is just a convenient instance.
     *
     * @since 1.343
     */
    public static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "anonymous", "anonymous", new GrantedAuthority[]{new GrantedAuthorityImpl("anonymous")});

    static {
        XSTREAM.alias("hudson", Hudson.class);
        XSTREAM.alias("slave", DumbSlave.class);
        XSTREAM.alias("jdk", JDK.class);
        // for backward compatibility with <1.75, recognize the tag name "view" as well.
        XSTREAM.alias("view", ListView.class);
        XSTREAM.alias("listView", ListView.class);
        // this seems to be necessary to force registration of converter early enough
        Mode.class.getEnumConstants();

        // double check that initialization order didn't do any harm
        assert PERMISSIONS != null;
        assert ADMINISTER != null;
    }
}
