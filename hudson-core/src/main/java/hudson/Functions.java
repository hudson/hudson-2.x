/*
 * The MIT License
 * 
 * Copyright (c) 2004-2011, Oracle Corporation, Kohsuke Kawaguchi, Nikita Levyankov
 * Yahoo! Inc., Stephen Connolly, Tom Huybrechts, Alan Harder, Romain Seguy
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
package hudson;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotatorFactory;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.model.ModelObject;
import hudson.model.Node;
import hudson.model.PageDecorator;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterDefinition.ParameterDescriptor;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.model.View;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.search.SearchableModelObject;
import hudson.security.AccessControlled;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.SecurityRealm;
import hudson.security.csrf.CrumbIssuer;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.Area;
import hudson.util.Iterators;
import hudson.util.Secret;
import hudson.views.MyViewsTabBar;
import hudson.views.ViewsTabBar;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jexl.parser.ASTSizeFunction;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.jelly.InternationalizedStringExpression;

/**
 * Utility functions used in views.
 *
 * <p>
 * An instance of this class is created for each request and made accessible
 * from view pages via the variable 'h' (h stands for Hudson.)
 *
 * @author Kohsuke Kawaguchi
 */
public class Functions {
    private static volatile int globalIota = 0;
    private int iota;

    public Functions() {
        iota = globalIota;
        // concurrent requests can use the same ID --- we are just trying to
        // prevent the same user from seeing the same ID repeatedly.
        globalIota+=1000;
    }

    /**
     * Generates an unique ID.
     */
    public String generateId() {
        return "id"+iota++;
    }

    public static boolean isModel(Object o) {
        return o instanceof ModelObject;
    }

    public static String xsDate(Calendar cal) {
        return Util.XS_DATETIME_FORMATTER.format(cal.getTime());
    }

    public static String rfc822Date(Calendar cal) {
        return Util.RFC822_DATETIME_FORMATTER.format(cal.getTime());
    }

    /**
     * Given {@code c=MyList (extends ArrayList<Foo>), base=List}, compute the parameterization of 'base'
     * that's assignable from 'c' (in this case {@code List<Foo>}), and return its n-th type parameter
     * (n=0 would return {@code Foo}).
     *
     * <p>
     * This method is useful for doing type arithmetic.
     *
     * @throws AssertionError
     *      if c' is not parameterized.
     */
    public static <B> Class getTypeParameter(Class<? extends B> c, Class<B> base, int n) {
        Type parameterization = Types.getBaseClass(c,base);
        if (parameterization instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) parameterization;
            return Types.erasure(Types.getTypeArgument(pt,n));
        } else {
            throw new AssertionError(c+" doesn't properly parameterize "+base);
        }
    }

    public JDK.DescriptorImpl getJDKDescriptor() {
        return Hudson.getInstance().getDescriptorByType(JDK.DescriptorImpl.class);
    }

    /**
     * Prints the integer as a string that represents difference,
     * like "-5", "+/-0", "+3".
     */
    public static String getDiffString(int i) {
        if(i==0)    return "\u00B10";   // +/-0
        String s = Integer.toString(i);
        if(i>0)     return "+"+s;
        else        return s;
    }

    /**
     * {@link #getDiffString(int)} that doesn't show anything for +/-0
     */
    public static String getDiffString2(int i) {
        if(i==0)    return "";
        String s = Integer.toString(i);
        if(i>0)     return "+"+s;
        else        return s;
    }

    /**
     * {@link #getDiffString2(int)} that puts the result into prefix and suffix
     * if there's something to print
     */
    public static String getDiffString2(String prefix, int i, String suffix) {
        if(i==0)    return "";
        String s = Integer.toString(i);
        if(i>0)     return prefix+"+"+s+suffix;
        else        return prefix+s+suffix;
    }

    /**
     * Adds the proper suffix.
     */
    public static String addSuffix(int n, String singular, String plural) {
        StringBuilder buf = new StringBuilder();
        buf.append(n).append(' ');
        if(n==1)
            buf.append(singular);
        else
            buf.append(plural);
        return buf.toString();
    }

    public static RunUrl decompose(StaplerRequest req) {
        List<Ancestor> ancestors = req.getAncestors();

        // find the first and last Run instances
        Ancestor f=null,l=null;
        for (Ancestor anc : ancestors) {
            if(anc.getObject() instanceof Run) {
                if(f==null) f=anc;
                l=anc;
            }
        }
        if(l==null) return null;    // there was no Run object

        String head = f.getPrev().getUrl()+'/';
        String base = l.getUrl();

        String reqUri = req.getOriginalRequestURI();
        // Find "rest" or URI by removing N path components.
        // Not using reqUri.substring(f.getUrl().length()) to avoid mismatches due to
        // url-encoding or extra slashes.  Former may occur in Tomcat (despite the spec saying
        // this string is not decoded, Tomcat apparently decodes this string. You see ' '
        // instead of '%20', which is what the browser has sent), latter may occur in some
        // proxy or URL-rewriting setups where extra slashes are inadvertently added.
        String furl = f.getUrl();
        int slashCount = 0;
        // Count components in ancestor URL
        for (int i = furl.indexOf('/'); i >= 0; i = furl.indexOf('/', i + 1)) slashCount++;
        // Remove that many from request URL, ignoring extra slashes
        String rest = reqUri.replaceFirst("(?:/+[^/]*){" + slashCount + "}", "");

        return new RunUrl( (Run) f.getObject(), head, base, rest);
    }

    /**
     * If we know the user's screen resolution, return it. Otherwise null.
     * @since 1.213
     */
    public static Area getScreenResolution() {
        Cookie res = Functions.getCookie(Stapler.getCurrentRequest(),"screenResolution");
        if(res!=null)
            return Area.parse(res.getValue());
        return null;
    }

    /**
     * URL decomposed for easier computation of relevant URLs.
     *
     * <p>
     * The decomposed URL will be of the form:
     * <pre>
     * aaaaaa/524/bbbbb/cccc
     * -head-| N |---rest---
     * ----- base -----|
     * </pre>
     *
     * <p>
     * The head portion is the part of the URL from the {@link Hudson}
     * object to the first {@link Run} subtype. When "next/prev build"
     * is chosen, this part remains intact.
     *
     * <p>
     * The <tt>524</tt> is the path from {@link Job} to {@link Run}.
     *
     * <p>
     * The <tt>bbb</tt> portion is the path after that till the last
     * {@link Run} subtype. The <tt>ccc</tt> portion is the part
     * after that.
     */
    public static final class RunUrl {
        private final String head, base, rest;
        private final Run run;


        public RunUrl(Run run, String head, String base, String rest) {
            this.run = run;
            this.head = head;
            this.base = base;
            this.rest = rest;
        }

        public String getBaseUrl() {
            return base;
        }

        /**
         * Returns the same page in the next build.
         */
        public String getNextBuildUrl() {
            return getUrl(run.getNextBuild());
        }

        /**
         * Returns the same page in the previous build.
         */
        public String getPreviousBuildUrl() {
            return getUrl(run.getPreviousBuild());
        }

        private String getUrl(Run n) {
            if(n ==null)
                return null;
            else {
                return head+n.getNumber()+rest;
            }
        }
    }

    public static Node.Mode[] getNodeModes() {
        return Node.Mode.values();
    }

    public static String getProjectListString(List<Project> projects) {
        return Items.toNameList(projects);
    }

    /**
     * @deprecated as of 1.294
     *      JEXL now supports the real ternary operator "x?y:z", so this work around
     *      is no longer necessary.
     */
    public static Object ifThenElse(boolean cond, Object thenValue, Object elseValue) {
        return cond ? thenValue : elseValue;
    }

    public static String appendIfNotNull(String text, String suffix, String nullText) {
        return text == null ? nullText : text + suffix;
    }

    public static Map getSystemProperties() {
        return new TreeMap<Object,Object>(System.getProperties());
    }

    public static Map getEnvVars() {
        return new TreeMap<String,String>(EnvVars.masterEnvVars);
    }

    public static boolean isWindows() {
        return File.pathSeparatorChar==';';
    }

    public static List<LogRecord> getLogRecords() {
        return Hudson.logRecords;
    }

    public static String printLogRecord(LogRecord r) {
        return formatter.format(r);
    }

    public static Cookie getCookie(HttpServletRequest req,String name) {
        Cookie[] cookies = req.getCookies();
        if(cookies!=null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static String getCookie(HttpServletRequest req,String name, String defaultValue) {
        Cookie c = getCookie(req, name);
        if(c==null || c.getValue()==null) return defaultValue;
        return c.getValue();
    }

    /**
     * Gets the suffix to use for YUI JavaScript.
     */
    public static String getYuiSuffix() {
        return DEBUG_YUI ? "debug" : "min";
    }

    /**
     * Set to true if you need to use the debug version of YUI.
     */
    public static boolean DEBUG_YUI = Boolean.getBoolean("debug.YUI");

    /**
     * Creates a sub map by using the given range (both ends inclusive).
     */
    public static <V> SortedMap<Integer,V> filter(SortedMap<Integer,V> map, String from, String to) {
        if(from==null && to==null)      return map;
        if(to==null)
            return map.headMap(Integer.parseInt(from)-1);
        if(from==null)
            return map.tailMap(Integer.parseInt(to));

        return map.subMap(Integer.parseInt(to),Integer.parseInt(from)-1);
    }

    private static final SimpleFormatter formatter = new SimpleFormatter();

    /**
     * Used by <tt>layout.jelly</tt> to control the auto refresh behavior.
     *
     * @param noAutoRefresh
     *      On certain pages, like a page with forms, will have annoying interference
     *      with auto refresh. On those pages, disable auto-refresh.
     */
    public static void configureAutoRefresh(HttpServletRequest request, HttpServletResponse response, boolean noAutoRefresh) {
        if(noAutoRefresh)
            return;

        String param = request.getParameter("auto_refresh");
        boolean refresh = isAutoRefresh(request);
        if (param != null) {
            refresh = Boolean.parseBoolean(param);
            Cookie c = new Cookie("hudson_auto_refresh", Boolean.toString(refresh));
            // Need to set path or it will not stick from e.g. a project page to the dashboard.
            // Using request.getContextPath() might work but it seems simpler to just use the hudson_ prefix
            // to avoid conflicts with any other web apps that might be on the same machine.
            c.setPath("/");
            c.setMaxAge(60*60*24*30); // persist it roughly for a month
            response.addCookie(c);
        }
        if (refresh) {
            response.addHeader("Refresh", System.getProperty("hudson.Functions.autoRefreshSeconds", "10"));
        }
    }

    public static boolean isAutoRefresh(HttpServletRequest request) {
        String param = request.getParameter("auto_refresh");
        if (param != null) {
            return Boolean.parseBoolean(param);
        }
        Cookie[] cookies = request.getCookies();
        if(cookies==null)
            return false; // when API design messes it up, we all suffer

        for (Cookie c : cookies) {
            if (c.getName().equals("hudson_auto_refresh")) {
                return Boolean.parseBoolean(c.getValue());
            }
        }
        return false;
    }

    /**
     * Finds the given object in the ancestor list and returns its URL.
     * This is used to determine the "current" URL assigned to the given object,
     * so that one can compute relative URLs from it.
     */
    public static String getNearestAncestorUrl(StaplerRequest req,Object it) {
        List list = req.getAncestors();
        for( int i=list.size()-1; i>=0; i-- ) {
            Ancestor anc = (Ancestor) list.get(i);
            if(anc.getObject()==it)
                return anc.getUrl();
        }
        return null;
    }

    /**
     * Finds the inner-most {@link SearchableModelObject} in scope.
     */
    public static String getSearchURL() {
        List list = Stapler.getCurrentRequest().getAncestors();
        for( int i=list.size()-1; i>=0; i-- ) {
            Ancestor anc = (Ancestor) list.get(i);
            if(anc.getObject() instanceof SearchableModelObject)
                return anc.getUrl()+"/search/";
        }
        return null;
    }

    public static String appendSpaceIfNotNull(String n) {
        if(n==null) return null;
        else        return n+' ';
    }

    /**
     * One nbsp per 10 pixels in given size, which may be a plain number or "NxN"
     * (like an iconSize).  Useful in a sortable table heading.
     */
    public static String nbspIndent(String size) {
        int i = size.indexOf('x');
        i = Integer.parseInt(i > 0 ? size.substring(0, i) : size) / 10;
        StringBuilder buf = new StringBuilder(30);
        for (int j = 0; j < i; j++)
            buf.append("&nbsp;");
        return buf.toString();
    }

    public static String getWin32ErrorMessage(IOException e) {
        return Util.getWin32ErrorMessage(e);
    }

    public static boolean isMultiline(String s) {
        if(s==null)     return false;
        return s.indexOf('\r')>=0 || s.indexOf('\n')>=0;
    }

    public static String encode(String s) {
        return Util.encode(s);
    }

    public static String escape(String s) {
        return Util.escape(s);
    }

    public static String xmlEscape(String s) {
        return Util.xmlEscape(s);
    }

    public static String xmlUnescape(String s) {
        return s.replace("&lt;","<").replace("&gt;",">").replace("&amp;","&");
    }

    public static void checkPermission(Permission permission) throws IOException, ServletException {
        checkPermission(Hudson.getInstance(),permission);
    }

    public static void checkPermission(AccessControlled object, Permission permission) throws IOException, ServletException {
        if (permission != null) {
            object.checkPermission(permission);
        }
    }

    /**
     * This version is so that the 'checkPermission' on <tt>layout.jelly</tt>
     * degrades gracefully if "it" is not an {@link AccessControlled} object.
     * Otherwise it will perform no check and that problem is hard to notice.
     */
    public static void checkPermission(Object object, Permission permission) throws IOException, ServletException {
        if (permission == null)
            return;

        if (object instanceof AccessControlled)
            checkPermission((AccessControlled) object,permission);
        else {
            List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
            for(Ancestor anc : Iterators.reverse(ancs)) {
                Object o = anc.getObject();
                if (o instanceof AccessControlled) {
                    checkPermission((AccessControlled) o,permission);
                    return;
                }
            }
            checkPermission(Hudson.getInstance(),permission);
        }
    }

    /**
     * Returns true if the current user has the given permission.
     *
     * @param permission
     *      If null, returns true. This defaulting is convenient in making the use of this method terse.
     */
    public static boolean hasPermission(Permission permission) throws IOException, ServletException {
        return hasPermission(Hudson.getInstance(),permission);
    }

    /**
     * This version is so that the 'hasPermission' can degrade gracefully
     * if "it" is not an {@link AccessControlled} object.
     */
    public static boolean hasPermission(Object object, Permission permission) throws IOException, ServletException {
        if (permission == null)
            return true;
        if (object instanceof AccessControlled)
            return ((AccessControlled)object).hasPermission(permission);
        else {
            List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
            for(Ancestor anc : Iterators.reverse(ancs)) {
                Object o = anc.getObject();
                if (o instanceof AccessControlled) {
                    return ((AccessControlled)o).hasPermission(permission);
                }
            }
            return Hudson.getInstance().hasPermission(permission);
        }
    }

    public static void adminCheck(StaplerRequest req, StaplerResponse rsp, Object required, Permission permission) throws IOException, ServletException {
        // this is legacy --- all views should be eventually converted to
        // the permission based model.
        if(required!=null && !Hudson.adminCheck(req,rsp)) {
            // check failed. commit the FORBIDDEN response, then abort.
            rsp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            rsp.getOutputStream().close();
            throw new ServletException("Unauthorized access");
        }

        // make sure the user owns the necessary permission to access this page.
        if(permission!=null)
            checkPermission(permission);
    }

    /**
     * Infers the hudson installation URL from the given request.
     */
    public static String inferHudsonURL(StaplerRequest req) {
        String rootUrl = Hudson.getInstance().getRootUrl();
        if(rootUrl !=null)
            // prefer the one explicitly configured, to work with load-balancer, frontend, etc.
            return rootUrl;
        StringBuilder buf = new StringBuilder();
        buf.append(req.getScheme()).append("://");
        buf.append(req.getServerName());
        if(req.getLocalPort()!=80)
            buf.append(':').append(req.getLocalPort());
        buf.append(req.getContextPath()).append('/');
        return buf.toString();
    }

    public static List<JobPropertyDescriptor> getJobPropertyDescriptors(Class<? extends Job> clazz) {
        return JobPropertyDescriptor.getPropertyDescriptors(clazz);
    }

    public static List<Descriptor<BuildWrapper>> getBuildWrapperDescriptors(AbstractProject<?,?> project) {
        return BuildWrappers.getFor(project);
    }

    public static List<Descriptor<SecurityRealm>> getSecurityRealmDescriptors() {
        return SecurityRealm.all();
    }

    public static List<Descriptor<AuthorizationStrategy>> getAuthorizationStrategyDescriptors() {
        return AuthorizationStrategy.all();
    }

    public static List<Descriptor<Builder>> getBuilderDescriptors(AbstractProject<?,?> project) {
        return BuildStepDescriptor.filter(Builder.all(), project.getClass());
    }

    public static List<Descriptor<Publisher>> getPublisherDescriptors(AbstractProject<?,?> project) {
        return BuildStepDescriptor.filter(Publisher.all(), project.getClass());
    }

    public static List<SCMDescriptor<?>> getSCMDescriptors(AbstractProject<?,?> project) {
        return SCM._for(project);
    }

    public static List<Descriptor<ComputerLauncher>> getComputerLauncherDescriptors() {
        return Hudson.getInstance().<ComputerLauncher,Descriptor<ComputerLauncher>>getDescriptorList(
            ComputerLauncher.class);
    }

    public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() {
        return RetentionStrategy.all();
    }

    public static List<ParameterDescriptor> getParameterDescriptors() {
        return ParameterDefinition.all();
    }

    public static List<Descriptor<ViewsTabBar>> getViewsTabBarDescriptors() {
        return ViewsTabBar.all();
    }

    public static List<Descriptor<MyViewsTabBar>> getMyViewsTabBarDescriptors() {
        return MyViewsTabBar.all();
    }

    public static List<NodePropertyDescriptor> getNodePropertyDescriptors(Class<? extends Node> clazz) {
        List<NodePropertyDescriptor> result = new ArrayList<NodePropertyDescriptor>();
        Collection<NodePropertyDescriptor> list = (Collection) Hudson.getInstance().getDescriptorList(
            NodeProperty.class);
        for (NodePropertyDescriptor npd : list) {
            if (npd.isApplicable(clazz)) {
                result.add(npd);
            }
        }
        return result;
    }

    private static final Set<String> globalConfigIgnoredDescriptors = new HashSet<String>();

    /**
     * Set of descriptor class names to ignore in the global /configure page.
     *
     * @since 2.1.0
     */
    public static Set<String> getGlobalConfigIgnoredDescriptors() {
        return globalConfigIgnoredDescriptors;
    }

    /**
     * Gets all the descriptors sorted by their inheritance tree of {@link Describable}
     * so that descriptors of similar types come nearby.
     */
    public static Collection<Descriptor> getSortedDescriptorsForGlobalConfig() {
        Map<String,Descriptor> r = new TreeMap<String, Descriptor>();
        for (Descriptor<?> d : Hudson.getInstance().getExtensionList(Descriptor.class)) {
            if (globalConfigIgnoredDescriptors.contains(d.getClass().getName())) {
                continue;
            }
            if (d.getGlobalConfigPage()==null)  continue;
            r.put(buildSuperclassHierarchy(d.clazz, new StringBuilder()).toString(),d);
        }
        return r.values();
    }

    private static StringBuilder buildSuperclassHierarchy(Class c, StringBuilder buf) {
        Class sc = c.getSuperclass();
        if (sc!=null)   buildSuperclassHierarchy(sc,buf).append(':');
        return buf.append(c.getName());
    }

    /**
     * Computes the path to the icon of the given action
     * from the context path.
     */
    public static String getIconFilePath(Action a) {
        String name = a.getIconFileName();
        if(name.startsWith("/"))
            return name.substring(1);
        else
            return "images/24x24/"+name;
    }

    /**
     * Works like JSTL build-in size(x) function,
     * but handle null gracefully.
     */
    public static int size2(Object o) throws Exception {
        if(o==null) return 0;
        return ASTSizeFunction.sizeOf(o,Introspector.getUberspect());
    }

    /**
     * Computes the relative path from the current page to the given item.
     */
    public static String getRelativeLinkTo(Item p) {
        Map<Object,String> ancestors = new HashMap<Object,String>();
        View view=null;

        StaplerRequest request = Stapler.getCurrentRequest();
        for( Ancestor a : request.getAncestors() ) {
            ancestors.put(a.getObject(),a.getRelativePath());
            if(a.getObject() instanceof View)
                view = (View) a.getObject();
        }

        String path = ancestors.get(p);
        if(path!=null)  return path;

        Item i=p;
        String url = "";
        while(true) {
            ItemGroup ig = i.getParent();
            url = i.getShortUrl()+url;

            if(ig==Hudson.getInstance()) {
                assert i instanceof TopLevelItem;
                if(view!=null && view.contains((TopLevelItem)i)) {
                    // if p and the current page belongs to the same view, then return a relative path
                    return ancestors.get(view)+'/'+url;
                } else {
                    // otherwise return a path from the root Hudson
                    return request.getContextPath()+'/'+p.getUrl();
                }
            }

            path = ancestors.get(ig);
            if(path!=null)  return path+'/'+url;

            assert ig instanceof Item; // if not, ig must have been the Hudson instance
            i = (Item) ig;
        }
    }

    public static Map<Thread,StackTraceElement[]> dumpAllThreads() {
        Map<Thread,StackTraceElement[]> sorted = new TreeMap<Thread,StackTraceElement[]>(new ThreadSorter());
        sorted.putAll(Thread.getAllStackTraces());
        return sorted;
    }

    @IgnoreJRERequirement
    public static ThreadInfo[] getThreadInfos() {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        return mbean.dumpAllThreads(mbean.isObjectMonitorUsageSupported(),mbean.isSynchronizerUsageSupported());
    }

    public static ThreadGroupMap sortThreadsAndGetGroupMap(ThreadInfo[] list) {
        ThreadGroupMap sorter = new ThreadGroupMap();
        Arrays.sort(list, sorter);
        return sorter;
    }

    // Common code for sorting Threads/ThreadInfos by ThreadGroup
    private static class ThreadSorterBase {
        protected Map<Long,String> map = new HashMap<Long,String>();

        private ThreadSorterBase() {
            ThreadGroup tg = Thread.currentThread().getThreadGroup();
            while (tg.getParent() != null) tg = tg.getParent();
            Thread[] threads = new Thread[tg.activeCount()*2];
            int threadsLen = tg.enumerate(threads, true);
            for (int i = 0; i < threadsLen; i++)
                map.put(threads[i].getId(), threads[i].getThreadGroup().getName());
        }

        protected int compare(long idA, long idB) {
            String tga = map.get(idA), tgb = map.get(idB);
            int result = (tga!=null?-1:0) + (tgb!=null?1:0);  // Will be non-zero if only one is null
            if (result==0 && tga!=null)
                result = tga.compareToIgnoreCase(tgb);
            return result;
        }
    }

    public static class ThreadGroupMap extends ThreadSorterBase implements Comparator<ThreadInfo> {

        /**
         * @return ThreadGroup name or null if unknown
         */
        public String getThreadGroup(ThreadInfo ti) {
            return map.get(ti.getThreadId());
        }

        public int compare(ThreadInfo a, ThreadInfo b) {
            int result = compare(a.getThreadId(), b.getThreadId());
            if (result == 0)
                result = a.getThreadName().compareToIgnoreCase(b.getThreadName());
            return result;
        }
    }

    private static class ThreadSorter extends ThreadSorterBase implements Comparator<Thread> {

        public int compare(Thread a, Thread b) {
            int result = compare(a.getId(), b.getId());
            if (result == 0)
                result = a.getName().compareToIgnoreCase(b.getName());
            return result;
        }
    }

    /**
     * Are we running on JRE6 or above?
     */
    @IgnoreJRERequirement
    public static boolean isMustangOrAbove() {
        try {
            System.console();
            return true;
        } catch(LinkageError e) {
            return false;
        }
    }

    // ThreadInfo.toString() truncates the stack trace by first 8, so needed my own version
    @IgnoreJRERequirement
    public static String dumpThreadInfo(ThreadInfo ti, ThreadGroupMap map) {
        String grp = map.getThreadGroup(ti);
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" +
                                             " Id=" + ti.getThreadId() + " Group=" +
                                             (grp != null ? grp : "?") + " " +
                                             ti.getThreadState());
        if (ti.getLockName() != null) {
            sb.append(" on " + ti.getLockName());
        }
        if (ti.getLockOwnerName() != null) {
            sb.append(" owned by \"" + ti.getLockOwnerName() +
                      "\" Id=" + ti.getLockOwnerId());
        }
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        StackTraceElement[] stackTrace = ti.getStackTrace();
        for (int i=0; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && ti.getLockInfo() != null) {
                Thread.State ts = ti.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + ti.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + ti.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + ti.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : ti.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
       }

       LockInfo[] locks = ti.getLockedSynchronizers();
       if (locks.length > 0) {
           sb.append("\n\tNumber of locked synchronizers = " + locks.length);
           sb.append('\n');
           for (LockInfo li : locks) {
               sb.append("\t- " + li);
               sb.append('\n');
           }
       }
       sb.append('\n');
       return sb.toString();
    }

    public static <T> Collection<T> emptyList() {
        return Collections.emptyList();
    }

    public static String jsStringEscape(String s) {
        StringBuilder buf = new StringBuilder();
        for( int i=0; i<s.length(); i++ ) {
            char ch = s.charAt(i);
            switch(ch) {
            case '\'':
                buf.append("\\'");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            case '"':
                buf.append("\\\"");
                break;
            default:
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     * Converts "abc" to "Abc".
     */
    public static String capitalize(String s) {
        if(s==null || s.length()==0) return s;
        return Character.toUpperCase(s.charAt(0))+s.substring(1);
    }

    public static String getVersion() {
        return Hudson.VERSION;
    }

    /**
     * Resoruce path prefix.
     */
    public static String getResourcePath() {
        return Hudson.RESOURCE_PATH;
    }

    public static String getViewResource(Object it, String path) {
        Class clazz = it.getClass();

        if(it instanceof Class)
            clazz = (Class)it;
        if(it instanceof Descriptor)
            clazz = ((Descriptor)it).clazz;

        StringBuilder buf = new StringBuilder(Stapler.getCurrentRequest().getContextPath());
        buf.append(Hudson.VIEW_RESOURCE_PATH).append('/');
        buf.append(clazz.getName().replace('.','/').replace('$','/'));
        buf.append('/').append(path);

        return buf.toString();
    }

    public static boolean hasView(Object it, String path) throws IOException {
        if(it==null)    return false;
        return Stapler.getCurrentRequest().getView(it,path)!=null;
    }

    /**
     * Can be used to check a checkbox by default.
     * Used from views like {@code h.defaultToTrue(scm.useUpdate)}.
     * The expression will evaluate to true if scm is null.
     */
    public static boolean defaultToTrue(Boolean b) {
        if(b==null) return true;
        return b;
    }

    /**
     * If the value exists, return that value. Otherwise return the default value.
     * <p>
     * Starting 1.294, JEXL supports the elvis operator "x?:y" that supercedes this.
     *
     * @since 1.150
     */
    public static <T> T defaulted(T value, T defaultValue) {
        return value!=null ? value : defaultValue;
    }

    public static String printThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Counts the number of rows needed for textarea to fit the content.
     * Minimum 5 rows.
     */
    public static int determineRows(String s) {
        if(s==null)     return 5;
        return Math.max(5,LINE_END.split(s).length);
    }

    /**
     * Converts the Hudson build status to CruiseControl build status,
     * which is either Success, Failure, Exception, or Unknown.
     */
    public static String toCCStatus(Item i) {
        if (i instanceof Job) {
            Job j = (Job) i;
            switch (j.getIconColor().noAnime()) {
            case ABORTED:
            case RED:
            case YELLOW:
                return "Failure";
            case BLUE:
                return "Success";
            case DISABLED:
            case GREY:
                return "Unknown";
            }
        }
        return "Unknown";
    }

    private static final Pattern LINE_END = Pattern.compile("\r?\n");

    /**
     * Checks if the current user is anonymous.
     */
    public static boolean isAnonymous() {
        return Hudson.getAuthentication() instanceof AnonymousAuthenticationToken;
    }

    /**
     * When called from within JEXL expression evaluation,
     * this method returns the current {@link JellyContext} used
     * to evaluate the script.
     *
     * @since 1.164
     */
    public static JellyContext getCurrentJellyContext() {
        JellyContext context = ExpressionFactory2.CURRENT_CONTEXT.get();
        assert context!=null;
        return context;
    }

    /**
     * Evaluate a Jelly script and return output as a String.
     *
     * @since 1.267
     */
    public static String runScript(Script script) throws JellyTagException {
        StringWriter out = new StringWriter();
        script.run(getCurrentJellyContext(), XMLOutput.createXMLOutput(out));
        return out.toString();
    }

    /**
     * Returns a sub-list if the given list is bigger than the specified 'maxSize'
     */
    public static <T> List<T> subList(List<T> base, int maxSize) {
        if(maxSize<base.size())
            return base.subList(0,maxSize);
        else
            return base;
    }

    /**
     * Computes the hyperlink to actions, to handle the situation when the {@link Action#getUrlName()}
     * returns absolute URL.
     */
    public static String getActionUrl(String itUrl,Action action) {
        String urlName = action.getUrlName();
        if(urlName==null)   return null;    // to avoid NPE and fail to render the whole page

        if(SCHEME.matcher(urlName).matches())
            return urlName; // absolute URL
        if(urlName.startsWith("/"))
            return Stapler.getCurrentRequest().getContextPath()+urlName;
        else
            // relative URL name
            return Stapler.getCurrentRequest().getContextPath()+'/'+itUrl+urlName;
    }

    /**
     * Escapes the character unsafe for e-mail address.
     * See http://en.wikipedia.org/wiki/E-mail_address for the details,
     * but here the vocabulary is even more restricted.
     */
    public static String toEmailSafeString(String projectName) {
        // TODO: escape non-ASCII characters
        StringBuilder buf = new StringBuilder(projectName.length());
        for( int i=0; i<projectName.length(); i++ ) {
            char ch = projectName.charAt(i);
            if(('a'<=ch && ch<='z')
            || ('z'<=ch && ch<='Z')
            || ('0'<=ch && ch<='9')
            || "-_.".indexOf(ch)>=0)
                buf.append(ch);
            else
                buf.append('_');    // escape
        }
        return projectName;
    }

    public String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * Obtains the host name of the Hudson server that clients can use to talk back to.
     * <p>
     * This is primarily used in <tt>slave-agent.jnlp.jelly</tt> to specify the destination
     * that the slaves talk to.
     */
    public String getServerName() {
        // Try to infer this from the configured root URL.
        // This makes it work correctly when Hudson runs behind a reverse proxy.
        String url = Hudson.getInstance().getRootUrl();
        try {
            if(url!=null) {
                String host = new URL(url).getHost();
                if(host!=null)
                    return host;
            }
        } catch (MalformedURLException e) {
            // fall back to HTTP request
        }
        return Stapler.getCurrentRequest().getServerName();
    }

    /**
     * Determines the form validation check URL. See textbox.jelly
     */
    public String getCheckUrl(String userDefined, Object descriptor, String field) {
        if(userDefined!=null || field==null)   return userDefined;
        if (descriptor instanceof Descriptor) {
            Descriptor d = (Descriptor) descriptor;
            return d.getCheckUrl(field);
        }
        return null;
    }

    /**
     * If the given href link is matching the current page, return true.
     *
     * Used in <tt>task.jelly</tt> to decide if the page should be highlighted.
     */
    public boolean hyperlinkMatchesCurrentPage(String href) throws UnsupportedEncodingException {
        String url = Stapler.getCurrentRequest().getRequestURL().toString();
        if (href == null || href.length() <= 1) return ".".equals(href) && url.endsWith("/");
        url = URLDecoder.decode(url,"UTF-8");
        href = URLDecoder.decode(href,"UTF-8");
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (href.endsWith("/")) href = href.substring(0, href.length() - 1);

        return url.endsWith(href);
    }

    public <T> List<T> singletonList(T t) {
        return Collections.singletonList(t);
    }

    /**
     * Gets all the {@link PageDecorator}s.
     */
    public static List<PageDecorator> getPageDecorators() {
        // this method may be called to render start up errors, at which point Hudson doesn't exist yet. see HUDSON-3608 
        if(Hudson.getInstance()==null)  return Collections.emptyList();
        return PageDecorator.all();
    }

    public static List<Descriptor<Cloud>> getCloudDescriptors() {
        return Cloud.all();
    }

    /**
     * Prepend a prefix only when there's the specified body.
     */
    public String prepend(String prefix, String body) {
        if(body!=null && body.length()>0)
            return prefix+body;
        return body;
    }

    public static List<Descriptor<CrumbIssuer>> getCrumbIssuerDescriptors() {
        return CrumbIssuer.all();
    }

    public static String getCrumb(StaplerRequest req) {
        Hudson h = Hudson.getInstance();
        CrumbIssuer issuer = h != null ? h.getCrumbIssuer() : null;
        return issuer != null ? issuer.getCrumb(req) : "";
    }

    public static String getCrumbRequestField() {
        Hudson h = Hudson.getInstance();
        CrumbIssuer issuer = h != null ? h.getCrumbIssuer() : null;
        return issuer != null ? issuer.getDescriptor().getCrumbRequestField() : "";
    }

    public static Date getCurrentTime() {
        return new Date();
    }

    public static Locale getClientLocale(){
        return Stapler.getCurrentRequest().getLocale();
    }

    public static Locale getServerLocale(){
        return Locale.getDefault();
    }

    /**
     * Generate a series of &lt;script> tags to include <tt>script.js</tt>
     * from {@link ConsoleAnnotatorFactory}s and {@link ConsoleAnnotationDescriptor}s.
     */
    public static String generateConsoleAnnotationScriptAndStylesheet() {
        String cp = Stapler.getCurrentRequest().getContextPath();
        StringBuilder buf = new StringBuilder();
        for (ConsoleAnnotatorFactory f : ConsoleAnnotatorFactory.all()) {
            String path = cp + "/extensionList/" + ConsoleAnnotatorFactory.class.getName() + "/" + f.getClass().getName();
            if (f.hasScript())
                buf.append("<script src='"+path+"/script.js'></script>");
            if (f.hasStylesheet())
                buf.append("<link rel='stylesheet' type='text/css' href='"+path+"/style.css' />");
        }
        for (ConsoleAnnotationDescriptor d : ConsoleAnnotationDescriptor.all()) {
            String path = cp+"/descriptor/"+d.clazz.getName();
            if (d.hasScript())
                buf.append("<script src='"+path+"/script.js'></script>");
            if (d.hasStylesheet())
                buf.append("<link rel='stylesheet' type='text/css' href='"+path+"/style.css' />");
        }
        return buf.toString();
    }

    /**
     * Work around for bug 6935026.
     */
    public List<String> getLoggerNames() {
        while (true) {
            try {
                List<String> r = new ArrayList<String>();
                Enumeration<String> e = LogManager.getLogManager().getLoggerNames();
                while (e.hasMoreElements())
                    r.add(e.nextElement());
                return r;
            } catch (ConcurrentModificationException e) {
                // retry
            }
        }
    }

    /**
     * Used by &lt;f:password/> so that we send an encrypted value to the client.
     */
    public String getPasswordValue(Object o) {
        if (o==null)    return null;
        if (o instanceof Secret)    return ((Secret)o).getEncryptedValue();
        return o.toString();
    }

    public List filterDescriptors(Object context, Iterable descriptors) {
        return DescriptorVisibilityFilter.apply(context,descriptors);
    }

    private static final Pattern SCHEME = Pattern.compile("[a-z]+://.+");

    /**
     * Returns true if we are running unit tests.
     */
    public static boolean getIsUnitTest() {
        return Main.isUnitTest;
    }

    /**
     * Returns {@code true} if the {@link Run#ARTIFACTS} permission is enabled,
     * {@code false} otherwise.
     *
     * <p>When the {@link Run#ARTIFACTS} permission is not turned on using the
     * {@code hudson.security.ArtifactsPermission}, this permission must not be
     * considered to be set to {@code false} for every user. It must rather be
     * like if the permission doesn't exist at all (which means that every user
     * has to have an access to the artifacts but the permission can't be
     * configured in the security screen). Got it?</p>
     */
    public static boolean isArtifactsPermissionEnabled() {
        return Boolean.getBoolean("hudson.security.ArtifactsPermission");
    }

    /**
     * Returns true if current user is the author of the job.
     * @param job job.
     * @return returns true if current user is the author of the job.
     */
    public static boolean isAuthor(Job job) {
        User user = User.current();
        return !(user == null || job == null || job.getCreatedBy() == null) && job.getCreatedBy().equals(user.getId());
    }

    /**
     * Resolves the target object for the given object.  If the object is a StaplerProxy, then return the proxy target.
     *
     * @since 2.1.0
     */
    public static Object resolveStaplerObject(final Object obj) {
        if (obj instanceof StaplerProxy) {
            return ((StaplerProxy)obj).getTarget();
        }
        return obj;
    }

    /**
     * Returns true if the {@link Item#WIPEOUT} permission is enabled.
     *
     * By default the "Wipe Out Workspace" action is available on job when user has {@link Item#BUILD} permission
     * (if user can trigger builds). If this behavior is not acceptable for project you can enable the
     * {@code hudson.security.WipeOutPermission} system property. It will add "WipeOut" permission checkbox into
     * permission control panel to manage "Wipe Out Workspace" action.
     *
     * @return true if the {@link Item#WIPEOUT} permission is enabled.
     */
    public static boolean isWipeOutPermissionEnabled() {
        return Boolean.getBoolean("hudson.security.WipeOutPermission");
    }

    /**
     * Returns item by name from the list.
     *
     * @param items for filtering.
     * @param name the name of the item.
     * @return template.
     */
    public static <T extends Item> T getItemByName(List<T> items, final String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        Iterable<T> templates = Iterables.filter(items, new Predicate<T>() {
            public boolean apply(T item) {
                return name.equalsIgnoreCase(item.getName());
            }
        });
        return templates.iterator().hasNext() ? templates.iterator().next() : null;
    }

    public static Object rawHtml(Object o) {
        return InternationalizedStringExpression.rawHtml(o);
    }
}
