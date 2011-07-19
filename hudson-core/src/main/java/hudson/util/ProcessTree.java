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

package hudson.util;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.SlaveComputer;
import hudson.util.ProcessTree.OSProcess;

import hudson.util.ProcessTreeRemoting.IOSProcess;
import hudson.util.ProcessTreeRemoting.IProcessTree;
import hudson.util.jna.NativeAccessException;
import hudson.util.jna.NativeUtils;
import hudson.util.jna.NativeProcess;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import java.util.logging.Logger;

/**
 * Represents a snapshot of the process tree of the current system.
 *
 * <p>
 * A {@link ProcessTree} is really conceptually a map from process ID to a {@link OSProcess} object.
 * When Hudson runs on platforms that support process introspection, this allows you to introspect
 * and do some useful things on processes. On other platforms, the implementation falls back to
 * "do nothing" behavior.
 *
 * <p>
 * {@link ProcessTree} is remotable.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.315
 */
public abstract class ProcessTree implements Iterable<OSProcess>, IProcessTree, Serializable {
    /**
     * To be filled in the constructor of the derived type.
     */
    protected final Map<Integer/*pid*/, OSProcess> processes = new HashMap<Integer, OSProcess>();

    /**
     * Lazily obtained {@link ProcessKiller}s to be applied on this process tree.
     */
    private transient volatile List<ProcessKiller> killers;

    // instantiation only allowed for subtypes in this class
    private ProcessTree() {}

    /**
     * Gets the process given a specific ID, or null if no such process exists.
     */
    public final OSProcess get(int pid) {
        return processes.get(pid);
    }

    /**
     * Lists all the processes in the system.
     */
    public final Iterator<OSProcess> iterator() {
        return processes.values().iterator();
    }

    /**
     * Try to convert {@link Process} into this process object
     * or null if it fails (for example, maybe the snapshot is taken after
     * this process has already finished.)
     */
    public abstract OSProcess get(Process proc);

    /**
     * Kills all the processes that have matching environment variables.
     *
     * <p>
     * In this method, the method is given a
     * "model environment variables", which is a list of environment variables
     * and their values that are characteristic to the launched process.
     * The implementation is expected to find processes
     * in the system that inherit these environment variables, and kill
     * them all. This is suitable for locating daemon processes
     * that cannot be tracked by the regular ancestor/descendant relationship.
     */
    public abstract void killAll(Map<String, String> modelEnvVars) throws InterruptedException;

    /**
     * Convenience method that does {@link #killAll(Map)} and {@link OSProcess#killRecursively()}.
     * This is necessary to reliably kill the process and its descendants, as some OS
     * may not implement {@link #killAll(Map)}.
     *
     * Either of the parameter can be null.
     */
    public void killAll(Process proc, Map<String, String> modelEnvVars) throws InterruptedException {
        LOGGER.fine("killAll: process="+proc+" and envs="+modelEnvVars);
        OSProcess p = get(proc);
        if(p!=null) p.killRecursively();
        if(modelEnvVars!=null)
            killAll(modelEnvVars);
    }

    /**
     * Obtains the list of killers.
     */
    /*package*/ final List<ProcessKiller> getKillers() throws InterruptedException {
        if (killers==null)
            try {
                killers = SlaveComputer.getChannelToMaster().call(new Callable<List<ProcessKiller>, IOException>() {
                    public List<ProcessKiller> call() throws IOException {
                        return new ArrayList<ProcessKiller>(ProcessKiller.all());
                    }
                });
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to obtain killers",e);
                killers = Collections.emptyList();
            }
        return killers;
    }

    /**
     * Represents a process.
     */
    public abstract class OSProcess implements IOSProcess, Serializable {
        final int pid;

        // instantiation only allowed for subtypes in this class
        private OSProcess(int pid) {
            this.pid = pid;
        }

        public final int getPid() {
            return pid;
        }
        /**
         * Gets the parent process. This method may return null, because
         * there's no guarantee that we are getting a consistent snapshot
         * of the whole system state.
         */
        public abstract OSProcess getParent();

        /*package*/ final ProcessTree getTree() {
            return ProcessTree.this;
        }

        /**
         * Immediate child processes.
         */
        public final List<OSProcess> getChildren() {
            List<OSProcess> r = new ArrayList<OSProcess>();
            for (OSProcess p : ProcessTree.this)
                if(p.getParent()==this)
                    r.add(p);
            return r;
        }

        /**
         * Kills this process.
         */
        public abstract void kill() throws InterruptedException;

        void killByKiller() throws InterruptedException {
            for (ProcessKiller killer : getKillers())
                try {
                    if (killer.kill(this))
                        break;
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to kill pid="+getPid(),e);
                }
        }

        /**
         * Kills this process and all the descendants.
         * <p>
         * Note that the notion of "descendants" is somewhat vague,
         * in the presence of such things like daemons. On platforms
         * where the recursive operation is not supported, this just kills
         * the current process. 
         */
        public abstract void killRecursively() throws InterruptedException;

        /**
         * Gets the command-line arguments of this process.
         *
         * <p>
         * On Windows, where the OS models command-line arguments as a single string, this method
         * computes the approximated tokenization.
         */
        public abstract List<String> getArguments();

        /**
         * Obtains the environment variables of this process.
         *
         * @return
         *      empty map if failed (for example because the process is already dead,
         *      or the permission was denied.)
         */
        public abstract EnvVars getEnvironmentVariables();

        /**
         * Given the environment variable of a process and the "model environment variable" that Hudson
         * used for launching the build, returns true if there's a match (which means the process should
         * be considered a descendant of a build.)
         */
        public final boolean hasMatchingEnvVars(Map<String,String> modelEnvVar) {
            if(modelEnvVar.isEmpty())
                // sanity check so that we don't start rampage.
                return false;

            SortedMap<String,String> envs = getEnvironmentVariables();
            for (Entry<String,String> e : modelEnvVar.entrySet()) {
                String v = envs.get(e.getKey());
                if(v==null || !v.equals(e.getValue()))
                    return false;   // no match
            }

            return true;
        }

        /**
         * Executes a chunk of code at the same machine where this process resides.
         */
        public <T> T act(ProcessCallable<T> callable) throws IOException, InterruptedException {
            return callable.invoke(this,Hudson.MasterComputer.localChannel);
        }

        Object writeReplace() {
            return new SerializedProcess(pid);
        }
    }

    /**
     * Serialized form of {@link OSProcess} is the PID and {@link ProcessTree}
     */
    private final class SerializedProcess implements Serializable {
        private final int pid;
        private static final long serialVersionUID = 1L;

        private SerializedProcess(int pid) {
            this.pid = pid;
        }

        Object readResolve() {
            return get(pid);
        }
    }

    /**
     * Code that gets executed on the machine where the {@link OSProcess} is local.
     * Used to act on {@link OSProcess}.
     *
     * @see OSProcess#act(ProcessCallable)
     */
    public static interface ProcessCallable<T> extends Serializable {
        /**
         * Performs the computational task on the node where the data is located.
         *
         * @param process
         *      {@link OSProcess} that represents the local process.
         * @param channel
         *      The "back pointer" of the {@link Channel} that represents the communication
         *      with the node from where the code was sent.
         */
        T invoke(OSProcess process, VirtualChannel channel) throws IOException;
    }


    /**
     * Gets the {@link ProcessTree} of the current system
     * that JVM runs in, or in the worst case return the default one
     * that's not capable of killing descendants at all.
     */
    public static ProcessTree get() {
        if(!enabled)
            return DEFAULT;

        try {
            if(File.pathSeparatorChar==';')
                return new Windows();

            String os = Util.fixNull(System.getProperty("os.name"));
            if(os.equals("Linux"))
                return new Linux();
            if(os.equals("SunOS"))
                return new Solaris();
            if(os.equals("Mac OS X"))
                return new Darwin();
        } catch (LinkageError e) {
            LOGGER.log(Level.WARNING,"Failed to load winp. Reverting to the default",e);
            enabled = false;
        }

        return DEFAULT;
    }

//
//
// implementation follows
//-------------------------------------------
//

    /**
     * Empty process list as a default value if the platform doesn't support it.
     */
    private static final ProcessTree DEFAULT = new Local() {
        public OSProcess get(final Process proc) {
            return new OSProcess(-1) {
                public OSProcess getParent() {
                    return null;
                }

                public void killRecursively() {
                    // fall back to a single process killer
                    proc.destroy();
                }

                public void kill() throws InterruptedException {
                    proc.destroy();
                    killByKiller();
                }

                public List<String> getArguments() {
                    return Collections.emptyList();
                }

                public EnvVars getEnvironmentVariables() {
                    return new EnvVars();
                }
            };
        }

        public void killAll(Map<String, String> modelEnvVars) {
            // no-op
        }
    };


    private static final class Windows extends Local {
        Windows() {
            try {
                for (final NativeProcess p : NativeUtils.getInstance().getWindowsProcesses()) {
                    int pid = p.getPid();
                    super.processes.put(pid,new OSProcess(pid) {
                        private EnvVars env;
                        private List<String> args;

                        public OSProcess getParent() {
                            // windows process doesn't have parent/child relationship
                            return null;
                        }

                        public void killRecursively() {
                            LOGGER.finer("Killing recursively " + getPid());
                            p.killRecursively();
                        }

                        public void kill() throws InterruptedException {
                            LOGGER.finer("Killing " + getPid());
                            p.kill();
                            killByKiller();
                        }

                        @Override
                        public synchronized List<String> getArguments() {
                            if(args==null)  args = Arrays.asList(QuotedStringTokenizer.tokenize(p.getCommandLine()));
                            return args;
                        }

                        @Override
                        public synchronized EnvVars getEnvironmentVariables() {
                            if(env==null)   env = new EnvVars(p.getEnvironmentVariables());
                            return env;
                        }
                    });

                }
            } catch (NativeAccessException exc) {
                LOGGER.log(Level.WARNING,"Failed to fetch Windows Native Processes", exc);
            }
        }

        @Override
        public OSProcess get(Process proc) {
            try {
                return get(NativeUtils.getInstance().getWindowsProcessId(proc));
            } catch (NativeAccessException exc) {
                LOGGER.log(Level.WARNING,"Failed to get Windows Native Process", exc);
            }
            return null;
        }

        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this) {
                if (p.getPid() < 10) {
                    continue;   // ignore system processes like "idle process"
                }
                LOGGER.finest("Considering to kill " + p.getPid());

                boolean matched;
                matched = p.hasMatchingEnvVars(modelEnvVars);

                if (matched) {
                    p.killRecursively();
                } else {
                    LOGGER.finest("Environment variable didn't match");
                }

            }
        }
    }

    static abstract class Unix extends Local {
        @Override
        public OSProcess get(Process proc) {
            try {
                return get((Integer) UnixReflection.PID_FIELD.get(proc));
            } catch (IllegalAccessException e) { // impossible
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            }
        }

        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this)
                if(p.hasMatchingEnvVars(modelEnvVars))
                    p.killRecursively();
        }
    }
    /**
     * {@link ProcessTree} based on /proc.
     */
    static abstract class ProcfsUnix extends Unix {
        ProcfsUnix() {
            File[] processes = new File("/proc").listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory();
                }
            });
            if(processes==null) {
                LOGGER.info("No /proc");
                return;
            }

            for (File p : processes) {
                int pid;
                try {
                    pid = Integer.parseInt(p.getName());
                } catch (NumberFormatException e) {
                    // other sub-directories
                    continue;
                }
                try {
                    this.processes.put(pid,createProcess(pid));
                } catch (IOException e) {
                    // perhaps the process status has changed since we obtained a directory listing
                }
            }
        }

        protected abstract OSProcess createProcess(int pid) throws IOException;
    }

    /**
     * A process.
     */
    public abstract class UnixProcess extends OSProcess {
        protected UnixProcess(int pid) {
            super(pid);
        }

        protected final File getFile(String relativePath) {
            return new File(new File("/proc/"+getPid()),relativePath);
        }

        /**
         * Tries to kill this process.
         */
        public void kill() throws InterruptedException {
            try {
                int pid = getPid();
                LOGGER.fine("Killing pid="+pid);
                UnixReflection.DESTROY_PROCESS.invoke(null, pid);
            } catch (IllegalAccessException e) {
                // this is impossible
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            } catch (InvocationTargetException e) {
                // tunnel serious errors
                if(e.getTargetException() instanceof Error)
                    throw (Error)e.getTargetException();
                // otherwise log and let go. I need to see when this happens
                LOGGER.log(Level.INFO, "Failed to terminate pid="+getPid(),e);
            }
            killByKiller();
        }

        public void killRecursively() throws InterruptedException {
            LOGGER.fine("Recursively killing pid="+getPid());
            for (OSProcess p : getChildren())
                p.killRecursively();
            kill();
        }

        /**
         * Obtains the argument list of this process.
         *
         * @return
         *      empty list if failed (for example because the process is already dead,
         *      or the permission was denied.)
         */
        public abstract List<String> getArguments();
    }

    /**
     * Reflection used in the Unix support.
     */
    private static final class UnixReflection {
        /**
         * Field to access the PID of the process.
         */
        private static final Field PID_FIELD;

        /**
         * Method to destroy a process, given pid.
         */
        private static final Method DESTROY_PROCESS;

        static {
            try {
                Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                PID_FIELD = clazz.getDeclaredField("pid");
                PID_FIELD.setAccessible(true);

                DESTROY_PROCESS = clazz.getDeclaredMethod("destroyProcess",int.class);
                DESTROY_PROCESS.setAccessible(true);
            } catch (ClassNotFoundException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            } catch (NoSuchFieldException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            } catch (NoSuchMethodException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            }
        }
    }


    static class Linux extends ProcfsUnix {
        protected LinuxProcess createProcess(int pid) throws IOException {
            return new LinuxProcess(pid);
        }

        class LinuxProcess extends UnixProcess {
            private int ppid = -1;
            private EnvVars envVars;
            private List<String> arguments;

            LinuxProcess(int pid) throws IOException {
                super(pid);

                BufferedReader r = new BufferedReader(new FileReader(getFile("status")));
                try {
                    String line;
                    while((line=r.readLine())!=null) {
                        line=line.toLowerCase(Locale.ENGLISH);
                        if(line.startsWith("ppid:")) {
                            ppid = Integer.parseInt(line.substring(5).trim());
                            break;
                        }
                    }
                } finally {
                    r.close();
                }
                if(ppid==-1)
                    throw new IOException("Failed to parse PPID from /proc/"+pid+"/status");
            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized List<String> getArguments() {
                if(arguments!=null)
                    return arguments;
                arguments = new ArrayList<String>();
                try {
                    byte[] cmdline = FileUtils.readFileToByteArray(getFile("cmdline"));
                    int pos=0;
                    for (int i = 0; i < cmdline.length; i++) {
                        byte b = cmdline[i];
                        if(b==0) {
                            arguments.add(new String(cmdline,pos,i-pos));
                            pos=i+1;
                        }
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }
                arguments = Collections.unmodifiableList(arguments);
                return arguments;
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if(envVars !=null)
                    return envVars;
                envVars = new EnvVars();
                try {
                    byte[] environ = FileUtils.readFileToByteArray(getFile("environ"));
                    int pos=0;
                    for (int i = 0; i < environ.length; i++) {
                        byte b = environ[i];
                        if(b==0) {
                            envVars.addLine(new String(environ,pos,i-pos));
                            pos=i+1;
                        }
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }
                return envVars;
            }
        }
    }

    /**
     * Implementation for Solaris that uses <tt>/proc</tt>.
     *
     * Amazingly, this single code works for both 32bit and 64bit Solaris, despite the fact
     * that does a lot of pointer manipulation and what not.
     */
    static class Solaris extends ProcfsUnix {
        protected OSProcess createProcess(final int pid) throws IOException {
            return new SolarisProcess(pid);
        }

        private class SolarisProcess extends UnixProcess {
            private final int ppid;
            /**
                 * Address of the environment vector. Even on 64bit Solaris this is still 32bit pointer.
             */
            private final int envp;
            /**
                 * Similarly, address of the arguments vector.
             */
            private final int argp;
            private final int argc;
            private EnvVars envVars;
            private List<String> arguments;

            private SolarisProcess(int pid) throws IOException {
                super(pid);

                RandomAccessFile psinfo = new RandomAccessFile(getFile("psinfo"),"r");
                try {
                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/procfs.h
                    //typedef struct psinfo {
                    //	int	pr_flag;	/* process flags */
                    //	int	pr_nlwp;	/* number of lwps in the process */
                    //	pid_t	pr_pid;	/* process id */
                    //	pid_t	pr_ppid;	/* process id of parent */
                    //	pid_t	pr_pgid;	/* process id of process group leader */
                    //	pid_t	pr_sid;	/* session id */
                    //	uid_t	pr_uid;	/* real user id */
                    //	uid_t	pr_euid;	/* effective user id */
                    //	gid_t	pr_gid;	/* real group id */
                    //	gid_t	pr_egid;	/* effective group id */
                    //	uintptr_t	pr_addr;	/* address of process */
                    //	size_t	pr_size;	/* size of process image in Kbytes */
                    //	size_t	pr_rssize;	/* resident set size in Kbytes */
                    //	dev_t	pr_ttydev;	/* controlling tty device (or PRNODEV) */
                    //	ushort_t	pr_pctcpu;	/* % of recent cpu time used by all lwps */
                    //	ushort_t	pr_pctmem;	/* % of system memory used by process */
                    //	timestruc_t	pr_start;	/* process start time, from the epoch */
                    //	timestruc_t	pr_time;	/* cpu time for this process */
                    //	timestruc_t	pr_ctime;	/* cpu time for reaped children */
                    //	char	pr_fname[PRFNSZ];	/* name of exec'ed file */
                    //	char	pr_psargs[PRARGSZ];	/* initial characters of arg list */
                    //	int	pr_wstat;	/* if zombie, the wait() status */
                    //	int	pr_argc;	/* initial argument count */
                    //	uintptr_t	pr_argv;	/* address of initial argument vector */
                    //	uintptr_t	pr_envp;	/* address of initial environment vector */
                    //	char	pr_dmodel;	/* data model of the process */
                    //	lwpsinfo_t	pr_lwp;	/* information for representative lwp */
                    //} psinfo_t;

                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/types.h
                    // for the size of the various datatype.

                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/cmd/ptools/pargs/pargs.c
                    // for how to read this information

                    psinfo.seek(8);
                    if(adjust(psinfo.readInt())!=pid)
                        throw new IOException("psinfo PID mismatch");   // sanity check
                    ppid = adjust(psinfo.readInt());

                    psinfo.seek(188);  // now jump to pr_argc
                    argc = adjust(psinfo.readInt());
                    argp = adjust(psinfo.readInt());
                    envp = adjust(psinfo.readInt());
                } finally {
                    psinfo.close();
                }
                if(ppid==-1)
                    throw new IOException("Failed to parse PPID from /proc/"+pid+"/status");

            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized List<String> getArguments() {
                if(arguments!=null)
                    return arguments;

                arguments = new ArrayList<String>(argc);

                try {
                    RandomAccessFile as = new RandomAccessFile(getFile("as"),"r");
                    if(LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading "+getFile("as"));
                    try {
                        for( int n=0; n<argc; n++ ) {
                            // read a pointer to one entry
                            as.seek(to64(argp+n*4));
                            int p = adjust(as.readInt());

                            arguments.add(readLine(as, p, "argv["+ n +"]"));
                        }
                    } finally {
                        as.close();
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }

                arguments = Collections.unmodifiableList(arguments);
                return arguments;
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if(envVars !=null)
                    return envVars;
                envVars = new EnvVars();

                try {
                    RandomAccessFile as = new RandomAccessFile(getFile("as"),"r");
                    if(LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading "+getFile("as"));
                    try {
                        for( int n=0; ; n++ ) {
                            // read a pointer to one entry
                            as.seek(to64(envp+n*4));
                            int p = adjust(as.readInt());
                            if(p==0)
                                break;  // completed the walk

                            // now read the null-terminated string
                            envVars.addLine(readLine(as, p, "env["+ n +"]"));
                        }
                    } finally {
                        as.close();
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }

                return envVars;
            }

            private String readLine(RandomAccessFile as, int p, String prefix) throws IOException {
                if(LOGGER.isLoggable(FINEST))
                    LOGGER.finest("Reading "+prefix+" at "+p);

                as.seek(to64(p));
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int ch,i=0;
                while((ch=as.read())>0) {
                    if((++i)%100==0 && LOGGER.isLoggable(FINEST))
                        LOGGER.finest(prefix +" is so far "+buf.toString());

                    buf.write(ch);
                }
                String line = buf.toString();
                if(LOGGER.isLoggable(FINEST))
                    LOGGER.finest(prefix+" was "+line);
                return line;
            }
        }

        /**
         * int to long conversion with zero-padding.
         */
        private static long to64(int i) {
            return i&0xFFFFFFFFL;
        }

        /**
         * {@link DataInputStream} reads a value in big-endian, so
         * convert it to the correct value on little-endian systems.
         */
        private static int adjust(int i) {
            if(IS_LITTLE_ENDIAN)
                return (i<<24) |((i<<8) & 0x00FF0000) | ((i>>8) & 0x0000FF00) | (i>>>24);
            else
                return i;
        }

    }

    /**
     * Implementation for Mac OS X based on sysctl(3).
     */
    private static class Darwin extends Unix {

        Darwin() {

            try {
                for (final NativeProcess process : NativeUtils.getInstance().getMacProcesses()) {
                    super.processes.put(process.getPid(), new DarwinProcess(process));
                }
            } catch (NativeAccessException exc) {
                LOGGER.log(Level.WARNING, "Failed to fecth Native Mac Processes", NativeUtils.getInstance().getLastMacError());
            }
        }

        private class DarwinProcess extends UnixProcess {
            NativeProcess process;
             
            private DarwinProcess(NativeProcess process) {
                super(process.getPid());
                this.process = process;
            }

            public OSProcess getParent() {
                return get(process.getPpid());
            }

            public synchronized EnvVars getEnvironmentVariables() {
                EnvVars envVars = new EnvVars();
                for (String key : process.getEnvironmentVariables().keySet()) {
                    envVars.put(key, process.getEnvironmentVariables().get(key)); 
                }
                return envVars;
            }

            public List<String> getArguments() {
                List<String> arguments = new ArrayList<String>();
                StringTokenizer tokenizer = new StringTokenizer(process.getCommandLine());
                // Skip the exec name
                tokenizer.nextToken();
                while (tokenizer.hasMoreTokens()){
                    arguments.add(tokenizer.nextToken());
                }
                return arguments;
            }
        }
    }

    /**
     * Represents a local process tree, where this JVM and the process tree run on the same system.
     * (The opposite of {@link Remote}.)
     */
    public static abstract class Local extends ProcessTree {
        Local() {
        }
    }

    /**
     * Represents a process tree over a channel.
     */
    public static class Remote extends ProcessTree implements Serializable {
        private final IProcessTree proxy;

        public Remote(ProcessTree proxy, Channel ch) {
            this.proxy = ch.export(IProcessTree.class,proxy);
            for (Entry<Integer,OSProcess> e : proxy.processes.entrySet())
                processes.put(e.getKey(),new RemoteProcess(e.getValue(),ch));
        }

        @Override
        public OSProcess get(Process proc) {
            return null;
        }

        @Override
        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            proxy.killAll(modelEnvVars);
        }

        Object writeReplace() {
            return this; // cancel out super.writeReplace()
        }
        
        private static final long serialVersionUID = 1L;

        private class RemoteProcess extends OSProcess implements Serializable {
            private final IOSProcess proxy;

            RemoteProcess(OSProcess proxy, Channel ch) {
                super(proxy.getPid());
                this.proxy = ch.export(IOSProcess.class,proxy);
            }

            public OSProcess getParent() {
                IOSProcess p = proxy.getParent();
                if (p==null)    return null;
                return get(p.getPid());
            }

            public void kill() throws InterruptedException {
                proxy.kill();
            }

            public void killRecursively() throws InterruptedException {
                proxy.killRecursively();
            }

            public List<String> getArguments() {
                return proxy.getArguments();
            }

            public EnvVars getEnvironmentVariables() {
                return proxy.getEnvironmentVariables();
            }

            Object writeReplace() {
                return this; // cancel out super.writeReplace()
            }

            public <T> T act(ProcessCallable<T> callable) throws IOException, InterruptedException {
                return proxy.act(callable);
            }


            private static final long serialVersionUID = 1L;
        }
    }

    /**
     * Use {@link Remote} as the serialized form.
     */
    /*package*/ Object writeReplace() {
        return new Remote(this,Channel.current());
    }

//    public static void main(String[] args) {
//        // dump everything
//        LOGGER.setLevel(Level.ALL);
//        ConsoleHandler h = new ConsoleHandler();
//        h.setLevel(Level.ALL);
//        LOGGER.addHandler(h);
//
//        Solaris killer = (Solaris)get();
//        Solaris.SolarisSystem s = killer.createSystem();
//        Solaris.SolarisProcess p = s.get(Integer.parseInt(args[0]));
//        System.out.println(p.getEnvVars());
//
//        if(args.length==2)
//            p.kill();
//    }

    /*
        On MacOS X, there's no procfs <http://www.osxbook.com/book/bonus/chapter11/procfs/>
        instead you'd do it with the sysctl <http://search.cpan.org/src/DURIST/Proc-ProcessTable-0.42/os/darwin.c>
        <http://developer.apple.com/documentation/Darwin/Reference/ManPages/man3/sysctl.3.html>

        There's CLI but that doesn't seem to offer the access to per-process info
        <http://developer.apple.com/documentation/Darwin/Reference/ManPages/man8/sysctl.8.html>



        On HP-UX, pstat_getcommandline get you command line, but I'm not seeing any environment variables.
     */

    private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
    private static final Logger LOGGER = Logger.getLogger(ProcessTree.class.getName());

    /**
     * Flag to control this feature.
     *
     * <p>
     * This feature involves some native code, so we are allowing the user to disable this
     * in case there's a fatal problem.
     *
     * <p>
     * This property supports two names for a compatibility reason.
     */
    public static boolean enabled = !Boolean.getBoolean(ProcessTreeKiller.class.getName()+".disable")
                                 && !Boolean.getBoolean(ProcessTree.class.getName()+".disable");
}
