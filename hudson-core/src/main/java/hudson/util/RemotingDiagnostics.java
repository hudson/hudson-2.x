/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi, CloudBees, Inc.
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
package hudson.util;

import groovy.lang.GroovyShell;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.VirtualChannel;
import hudson.security.AccessControlled;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Various remoting operations related to diagnostics.
 *
 * <p>
 * These code are useful wherever {@link VirtualChannel} is used, such as master, slaves, Maven JVMs, etc.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.175
 */
public final class RemotingDiagnostics {
    public static Map<Object,Object> getSystemProperties(VirtualChannel channel) throws IOException, InterruptedException {
        if(channel==null)
            return Collections.<Object,Object>singletonMap("N/A","N/A");
        return channel.call(new GetSystemProperties());
    }

    private static final class GetSystemProperties implements Callable<Map<Object,Object>,RuntimeException> {
        public Map<Object,Object> call() {
            return new TreeMap<Object,Object>(System.getProperties());
        }
        private static final long serialVersionUID = 1L;
    }

    public static Map<String,String> getThreadDump(VirtualChannel channel) throws IOException, InterruptedException {
        if(channel==null)
            return Collections.singletonMap("N/A","N/A");
        return channel.call(new GetThreadDump());
    }

    private static final class GetThreadDump implements Callable<Map<String,String>,RuntimeException> {
        public Map<String,String> call() {
            Map<String,String> r = new LinkedHashMap<String,String>();
            try {
                ThreadInfo[] data = Functions.getThreadInfos();
                Functions.ThreadGroupMap map = Functions.sortThreadsAndGetGroupMap(data);
                for (ThreadInfo ti : data)
                    r.put(ti.getThreadName(),Functions.dumpThreadInfo(ti,map));
            } catch (LinkageError _) {
                // not in JDK6. fall back to JDK5
                r.clear();
                for (Map.Entry<Thread,StackTraceElement[]> t : Functions.dumpAllThreads().entrySet()) {
                    StringBuilder buf = new StringBuilder();
                    for (StackTraceElement e : t.getValue())
                        buf.append(e).append('\n');
                    r.put(t.getKey().getName(),buf.toString());
                }
            }
            return r;
        }
        private static final long serialVersionUID = 1L;
    }

    /**
     * Executes Groovy script remotely.
     */
    public static String executeGroovy(String script, VirtualChannel channel) throws IOException, InterruptedException {
        return channel.call(new Script(script));
    }

    private static final class Script implements DelegatingCallable<String,RuntimeException> {
        private final String script;
        private transient ClassLoader cl;

        private Script(String script) {
            this.script = script;
            cl = getClassLoader();
        }

        public ClassLoader getClassLoader() {
            return Hudson.getInstance().getPluginManager().uberClassLoader;
        }

        public String call() throws RuntimeException {
            // if we run locally, cl!=null. Otherwise the delegating classloader will be available as context classloader.
            if (cl==null)       cl = Thread.currentThread().getContextClassLoader();
            GroovyShell shell = new GroovyShell(cl);

            StringWriter out = new StringWriter();
            PrintWriter pw = new PrintWriter(out);
            shell.setVariable("out", pw);
            try {
                Object output = shell.evaluate(script);
                if(output!=null)
                pw.print(output);
            } catch (Throwable t) {
                t.printStackTrace(pw);
            }
            return out.toString();
        }
    }

    /**
     * Obtains the heap dump in an HPROF file.
     */
    public static FilePath getHeapDump(VirtualChannel channel) throws IOException, InterruptedException {
        return channel.call(new Callable<FilePath, IOException>() {
            public FilePath call() throws IOException {
                final File hprof = File.createTempFile("hudson-heapdump", "hprof");
                hprof.delete();
                try {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    server.invoke(new ObjectName("com.sun.management:type=HotSpotDiagnostic"), "dumpHeap",
                            new Object[]{hprof.getAbsolutePath(), true}, new String[]{String.class.getName(), boolean.class.getName()});

                    return new FilePath(hprof);
                } catch (JMException e) {
                    throw new IOException2(e);
                }
            }

            private static final long serialVersionUID = 1L;
        });
    }

    /**
     * Heap dump, exposable to URL via Stapler.
     *
     */
    public static class HeapDump {
        private final AccessControlled owner;
        private final VirtualChannel channel;

        public HeapDump(AccessControlled owner, VirtualChannel channel) {
            this.owner = owner;
            this.channel = channel;
        }

        /**
         * Obtains the heap dump.
         */
        public void doIndex(StaplerResponse rsp) throws IOException {
            rsp.sendRedirect("heapdump.hprof");
        }

        @WebMethod(name="heapdump.hprof")
        public void doHeapDump(StaplerRequest req, StaplerResponse rsp) throws IOException, InterruptedException {
            owner.checkPermission(Hudson.ADMINISTER);
            rsp.setContentType("application/octet-stream");

            FilePath dump = obtain();
            try {
                dump.copyTo(rsp.getCompressedOutputStream(req));
            } finally {
                dump.delete();
            }
        }

        public FilePath obtain() throws IOException, InterruptedException {
            return RemotingDiagnostics.getHeapDump(channel);
        }
    }
}
