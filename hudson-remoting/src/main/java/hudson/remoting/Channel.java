/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package hudson.remoting;

import hudson.remoting.ExportTable.ExportList;
import hudson.remoting.PipeWindow.Key;
import hudson.remoting.PipeWindow.Real;
import hudson.remoting.forward.ForwarderFactory;
import hudson.remoting.forward.ListeningPort;
import hudson.remoting.forward.PortForwarder;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a communication channel to the remote peer.
 * <p/>
 * <p/>
 * A {@link Channel} is a mechanism for two JVMs to communicate over
 * bi-directional {@link InputStream}/{@link OutputStream} pair.
 * {@link Channel} represents an endpoint of the stream, and thus
 * two {@link Channel}s are always used in a pair.
 * <p/>
 * <p/>
 * Communication is established as soon as two {@link Channel} instances
 * are created at the end fo the stream pair
 * until the stream is terminated via {@link #close()}.
 * <p/>
 * <p/>
 * The basic unit of remoting is an executable {@link Callable} object.
 * An application can create a {@link Callable} object, and execute it remotely
 * by using the {@link #call(Callable)} method or {@link #callAsync(Callable)} method.
 * <p/>
 * <p/>
 * In this sense, {@link Channel} is a mechanism to delegate/offload computation
 * to other JVMs and somewhat like an agent system. This is bit different from
 * remoting technologies like CORBA or web services, where the server exposes a
 * certain functionality that clients invoke.
 * <p/>
 * <p/>
 * {@link Callable} object, as well as the return value / exceptions,
 * are transported by using Java serialization. All the necessary class files
 * are also shipped over {@link Channel} on-demand, so there's no need to
 * pre-deploy such classes on both JVMs.
 * <p/>
 * <p/>
 * <h2>Implementor's Note</h2>
 * <p/>
 * {@link Channel} builds its features in a layered model. Its higher-layer
 * features are built on top of its lower-layer features, and they
 * are called layer-0, layer-1, etc.
 * <p/>
 * <ul>
 * <li>
 * <b>Layer 0</b>:
 * See {@link Command} for more details. This is for higher-level features,
 * and not likely useful for applications directly.
 * <li>
 * <b>Layer 1</b>:
 * See {@link Request} for more details. This is for higher-level features,
 * and not likely useful for applications directly.
 * </ul>
 *
 * @author Kohsuke Kawaguchi, Winston Prakash (bug fixes)
 */
public class Channel implements VirtualChannel, IChannel {
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    /**
     * Human readable description of where this channel is connected to. Used during diagnostic output
     * and error reports.
     */
    private final String name;
    /*package*/ final boolean isRestricted;
    /*package*/ final ExecutorService executor;

    /**
     * If non-null, the incoming link is already shut down,
     * and reader is already terminated. The {@link Throwable} object indicates why the outgoing channel
     * was closed.
     */
    private volatile Throwable inClosed = null;
    /**
     * If non-null, the outgoing link is already shut down,
     * and no command can be sent. The {@link Throwable} object indicates why the outgoing channel
     * was closed.
     */
    private volatile Throwable outClosed = null;

    /*package*/ final Map<Integer, Request<?, ?>> pendingCalls = new Hashtable<Integer, Request<?, ?>>();

    /**
     * Records the {@link Request}s being executed on this channel, sent by the remote peer.
     */
    /*package*/ final Map<Integer, Request<?, ?>> executingCalls =
        Collections.synchronizedMap(new Hashtable<Integer, Request<?, ?>>());

    /**
     * {@link ClassLoader}s that are proxies of the remote classloaders.
     */
    /*package*/ final ImportedClassLoaderTable importedClassLoaders = new ImportedClassLoaderTable(this);

    /**
     * Objects exported via {@link #export(Class, Object)}.
     */
    private final ExportTable<Object> exportedObjects = new ExportTable<Object>();

    /**
     * {@link PipeWindow}s keyed by their OIDs (of the OutputStream exported by the other side.)
     * <p/>
     * <p/>
     * To make the GC of {@link PipeWindow} automatic, the use of weak references here are tricky.
     * A strong reference to {@link PipeWindow} is kept from {@link ProxyOutputStream}, and
     * this is the only strong reference. Thus while {@link ProxyOutputStream} is alive,
     * it keeps {@link PipeWindow} referenced, which in turn keeps its {@link PipeWindow.Real#key}
     * referenced, hence this map can be looked up by the OID. When the {@link ProxyOutputStream}
     * will be gone, the key is no longer strongly referenced, so it'll get cleaned up.
     * <p/>
     * <p/>
     * In some race condition situation, it might be possible for us to lose the tracking of the collect
     * window size. But as long as we can be sure that there's only one {@link PipeWindow} instance
     * per OID, it will only result in a temporary spike in the effective window size,
     * and therefore should be OK.
     */
    private final WeakHashMap<PipeWindow.Key, WeakReference<PipeWindow>> pipeWindows
        = new WeakHashMap<PipeWindow.Key, WeakReference<PipeWindow>>();

    /**
     * Registered listeners.
     */
    private final Vector<Listener> listeners = new Vector<Listener>();
    private int gcCounter;

    /**
     * Total number of nanoseconds spent for remote class loading.
     * <p/>
     * Remote code execution often results in classloading activity
     * (more precisely, when the remote peer requests some computation
     * on this channel, this channel often has to load necessary
     * classes from the remote peer.)
     * <p/>
     * This counter represents the total amount of time this channel
     * had to spend loading classes from the remote peer. The time
     * measurement doesn't include the time locally spent to actually
     * define the class (as the local classloading would have incurred
     * the same cost.)
     */
    public final AtomicLong classLoadingTime = new AtomicLong();

    /**
     * Total counts of remote classloading activities. Used in a pair
     * with {@link #classLoadingTime}.
     */
    public final AtomicInteger classLoadingCount = new AtomicInteger();

    /**
     * Total number of nanoseconds spent for remote resource loading.
     *
     * @see #classLoadingTime
     */
    public final AtomicLong resourceLoadingTime = new AtomicLong();

    /**
     * Total count of remote resource loading.
     *
     * @see #classLoadingCount
     */
    public final AtomicInteger resourceLoadingCount = new AtomicInteger();

    /**
     * Property bag that contains application-specific stuff.
     */
    private final Hashtable<Object, Object> properties = new Hashtable<Object, Object>();

    /**
     * Proxy to the remote {@link Channel} object.
     */
    private IChannel remoteChannel;

    /**
     * Capability of the remote {@link Channel}.
     */
    public final Capability remoteCapability;

    /**
     * When did we receive any data from this slave the last time?
     * This can be used as a basis for detecting dead connections.
     * <p/>
     * Note that this doesn't include our sender side of the operation,
     * as successfully returning from {@link #send(Command)} doesn't mean
     * anything in terms of whether the underlying network was able to send
     * the data (for example, if the other end of a socket connection goes down
     * without telling us anything, the {@link SocketOutputStream#write(int)} will
     * return right away, and the socket only really times out after 10s of minutes.
     */
    private volatile long lastHeard;

    /*package*/ final ExecutorService pipeWriter;


    /**
     * Communication mode.
     *
     * @since 1.161
     */
    public enum Mode {
        /**
         * Send binary data over the stream. Most efficient.
         */
        BINARY(new byte[]{0, 0, 0, 0}),
        /**
         * Send ASCII over the stream. Uses base64, so the efficiency goes down by 33%,
         * but this is useful where stream is binary-unsafe, such as telnet.
         */
        TEXT("<===[HUDSON TRANSMISSION BEGINS]===>") {
            @Override
            protected OutputStream wrap(OutputStream os) {
                return BinarySafeStream.wrap(os);
            }

            @Override
            protected InputStream wrap(InputStream is) {
                return BinarySafeStream.wrap(is);
            }
        },
        /**
         * Let the remote peer decide the transmission mode and follow that.
         * Note that if both ends use NEGOTIATE, it will dead lock.
         */
        NEGOTIATE(new byte[0]);

        /**
         * Preamble used to indicate the tranmission mode.
         * Because of the algorithm we use to detect the preamble,
         * the string cannot be any random string. For example,
         * if the preamble is "AAB", we'll fail to find a preamble
         * in "AAAB".
         */
        private final byte[] preamble;

        Mode(String preamble) {
            try {
                this.preamble = preamble.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
        }

        Mode(byte[] preamble) {
            this.preamble = preamble;
        }

        protected OutputStream wrap(OutputStream os) {
            return os;
        }

        protected InputStream wrap(InputStream is) {
            return is;
        }
    }

    public Channel(String name, ExecutorService exec, InputStream is, OutputStream os) throws IOException {
        this(name, exec, Mode.BINARY, is, os, null);
    }

    public Channel(String name, ExecutorService exec, Mode mode, InputStream is, OutputStream os) throws IOException {
        this(name, exec, mode, is, os, null);
    }

    public Channel(String name, ExecutorService exec, InputStream is, OutputStream os, OutputStream header)
        throws IOException {
        this(name, exec, Mode.BINARY, is, os, header);
    }

    public Channel(String name, ExecutorService exec, Mode mode, InputStream is, OutputStream os, OutputStream header)
        throws IOException {
        this(name, exec, mode, is, os, header, false);
    }

    /**
     * Creates a new channel.
     *
     * @param name Human readable name of this channel. Used for debug/logging. Can be anything.
     * @param exec Commands sent from the remote peer will be executed by using this {@link Executor}.
     * @param mode The encoding to be used over the stream.
     * @param is Stream connected to the remote peer. It's the caller's responsibility to do
     * buffering on this stream, if that's necessary.
     * @param os Stream connected to the remote peer. It's the caller's responsibility to do
     * buffering on this stream, if that's necessary.
     * @param header If non-null, receive the portion of data in <tt>is</tt> before
     * the data goes into the "binary mode". This is useful
     * when the established communication channel might include some data that might
     * be useful for debugging/trouble-shooting.
     * @param restricted If true, this channel won't accept {@link Command}s that allow the remote end to execute arbitrary closures
     * --- instead they can only call methods on objects that are exported by this channel.
     * This also prevents the remote end from loading classes into JVM.
     * <p/>
     * Note that it still allows the remote end to deserialize arbitrary object graph
     * (provided that all the classes are already available in this JVM), so exactly how
     * safe the resulting behavior is is up to discussion.
     */
    public Channel(String name, ExecutorService exec, Mode mode, InputStream is, OutputStream os, OutputStream header,
                   boolean restricted) throws IOException {
        this(name, exec, mode, is, os, header, restricted, new Capability());
    }

    /*package*/ Channel(String name, ExecutorService exec, Mode mode, InputStream is, OutputStream os,
                        OutputStream header, boolean restricted, Capability capability) throws IOException {
        this.name = name;
        this.executor = exec;
        this.isRestricted = restricted;

        if (export(this, false) != 1) {
            throw new AssertionError(); // export number 1 is reserved for the channel itself
        }
        remoteChannel = RemoteInvocationHandler.wrap(this, 1, IChannel.class, false, false);

        // write the magic preamble.
        // certain communication channel, such as forking JVM via ssh,
        // may produce some garbage at the beginning (for example a remote machine
        // might print some warning before the program starts outputting its own data.)
        //
        // so use magic preamble and discard all the data up to that to improve robustness.

        capability.writePreamble(os);

        ObjectOutputStream oos = null;
        if (mode != Mode.NEGOTIATE) {
            os.write(mode.preamble);
            oos = new ObjectOutputStream(mode.wrap(os));
            oos.flush();    // make sure that stream preamble is sent to the other end. avoids dead-lock
        }

        {// read the input until we hit preamble
            Mode[] modes = {Mode.BINARY, Mode.TEXT};
            byte[][] preambles = new byte[][]{Mode.BINARY.preamble, Mode.TEXT.preamble, Capability.PREAMBLE};
            int[] ptr = new int[3];
            Capability cap = new Capability(
                0); // remote capacity that we obtained. If we don't hear from remote, assume no capability

            while (true) {
                int ch = is.read();
                if (ch == -1) {
                    throw new EOFException("unexpected stream termination");
                }

                for (int i = 0; i < preambles.length; i++) {
                    byte[] preamble = preambles[i];
                    if (preamble[ptr[i]] == ch) {
                        if (++ptr[i] == preamble.length) {
                            switch (i) {
                                case 0:
                                case 1:
                                    // transmission mode negotiation
                                    if (mode == Mode.NEGOTIATE) {
                                        // now we know what the other side wants, so send the consistent preamble
                                        mode = modes[i];
                                        os.write(mode.preamble);
                                        oos = new ObjectOutputStream(mode.wrap(os));
                                        oos.flush();
                                    } else {
                                        if (modes[i] != mode) {
                                            throw new IOException("Protocol negotiation failure");
                                        }
                                    }
                                    this.oos = oos;
                                    this.remoteCapability = cap;
                                    this.pipeWriter = createPipeWriter();
                                    this.ois = new ObjectInputStream(mode.wrap(is));
                                    new ReaderThread(name).start();

                                    return;
                                case 2:
                                    cap = Capability.read(is);
                                    break;
                            }
                            ptr[i] = 0; // reset
                        }
                    } else {
                        // didn't match.
                        ptr[i] = 0;
                    }
                }

                if (header != null) {
                    header.write(ch);
                }
            }
        }
    }

    /**
     * Callback "interface" for changes in the state of {@link Channel}.
     */
    public static abstract class Listener {
        /**
         * When the channel was closed normally or abnormally due to an error.
         *
         * @param cause if the channel is closed abnormally, this parameter
         * represents an exception that has triggered it.
         * Otherwise null.
         */
        public void onClosed(Channel channel, IOException cause) {
        }
    }

    /*package*/ boolean isOutClosed() {
        return outClosed != null;
    }

    /**
     * Creates the {@link ExecutorService} for writing to pipes.
     * <p/>
     * <p/>
     * If the throttling is supported, use a separate thread to free up the main channel
     * reader thread (thus prevent blockage.) Otherwise let the channel reader thread do it,
     * which is the historical behaviour.
     */
    private ExecutorService createPipeWriter() {
        if (remoteCapability.supportsPipeThrottling()) {
            return Executors.newSingleThreadExecutor(new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Pipe writer thread: " + name);
                }
            });
        }
        return new SynchronousExecutorService();
    }

    /**
     * Sends a command to the remote end and executes it there.
     * <p/>
     * <p/>
     * This is the lowest layer of abstraction in {@link Channel}.
     * {@link Command}s are executed on a remote system in the order they are sent.
     */
    /*package*/
    synchronized void send(Command cmd) throws IOException {
        if (outClosed != null) {
            throw new ChannelClosedException(outClosed);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Send " + cmd);
        }
        Channel old = Channel.setCurrent(this);
        try {
            oos.writeObject(cmd);
            oos.flush();        // make sure the command reaches the other end.
        } finally {
            Channel.setCurrent(old);
        }
        // unless this is the last command, have OOS and remote OIS forget all the objects we sent
        // in this command. Otherwise it'll keep objects in memory unnecessarily.
        // However, this may fail if the command was the close, because that's supposed to be the last command
        // ever sent. See the comment from jglick on HUDSON-3077 about what happens if we do oos.reset(). 
        if (!(cmd instanceof CloseCommand)) {
            oos.reset();
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> T export(Class<T> type, T instance) {
        return export(type, instance, true);
    }

    /**
     * @param userProxy If true, the returned proxy will be capable to handle classes
     * defined in the user classloader as parameters and return values.
     * Such proxy relies on {@link RemoteClassLoader} and related mechanism,
     * so it's not usable for implementing lower-layer services that are
     * used by {@link RemoteClassLoader}.
     * <p/>
     * To create proxies for objects inside remoting, pass in false.
     */
    /*package*/ <T> T export(Class<T> type, T instance, boolean userProxy) {
        if (instance == null) {
            return null;
        }

        // every so often perform GC on the remote system so that
        // unused RemoteInvocationHandler get released, which triggers
        // unexport operation.
        if ((++gcCounter) % 10000 == 0) {
            try {
                send(new GCCommand());
            } catch (IOException e) {
                // for compatibility reason we can't change the export method signature
                logger.log(Level.WARNING, "Unable to send GC command", e);
            }
        }

        // proxy will unexport this instance when it's GC-ed on the remote machine.
        final int id = export(instance);
        return RemoteInvocationHandler.wrap(null, id, type, userProxy, exportedObjects.isRecording());
    }

    /*package*/ int export(Object instance) {
        return exportedObjects.export(instance);
    }

    /*package*/ int export(Object instance, boolean automaticUnexport) {
        return exportedObjects.export(instance, automaticUnexport);
    }

    /*package*/ Object getExportedObject(int oid) {
        return exportedObjects.get(oid);
    }

    /*package*/ void unexport(int id) {
        exportedObjects.unexportByOid(id);
    }

    /**
     * Preloads jar files on the remote side.
     * <p/>
     * <p/>
     * This is a performance improvement method that can be safely
     * ignored if your goal is just to make things working.
     * <p/>
     * <p/>
     * Normally, classes are transferred over the network one at a time,
     * on-demand. This design is mainly driven by how Java classloading works
     * &mdash; we can't predict what classes will be necessarily upfront very easily.
     * <p/>
     * <p/>
     * Classes are loaded only once, so for long-running {@link Channel},
     * this is normally an acceptable overhead. But sometimes, for example
     * when a channel is short-lived, or when you know that you'll need
     * a majority of classes in certain jar files, then it is more efficient
     * to send a whole jar file over the network upfront and thereby
     * avoiding individual class transfer over the network.
     * <p/>
     * <p/>
     * That is what this method does. It ensures that a series of jar files
     * are copied to the remote side (AKA "preloading.")
     * Classloading will consult the preloaded jars before performing
     * network transfer of class files.
     *
     * @param classLoaderRef This parameter is used to identify the remote classloader
     * that will prefetch the specified jar files. That is, prefetching
     * will ensure that prefetched jars will kick in
     * when this {@link Callable} object is actually executed remote side.
     * <p/>
     * <p/>
     * {@link RemoteClassLoader}s are created wisely, one per local {@link ClassLoader},
     * so this parameter doesn't have to be exactly the same {@link Callable}
     * to be executed later &mdash; it just has to be of the same class.
     * @param classesInJar {@link Class} objects that identify jar files to be preloaded.
     * Jar files that contain the specified classes will be preloaded into the remote peer.
     * You just need to specify one class per one jar.
     * @return true if the preloading actually happened. false if all the jars
     *         are already preloaded. This method is implemented in such a way that
     *         unnecessary jar file transfer will be avoided, and the return value
     *         will tell you if this optimization kicked in. Under normal circumstances
     *         your program shouldn't depend on this return value. It's just a hint.
     * @throws IOException if the preloading fails.
     */
    public boolean preloadJar(Callable<?, ?> classLoaderRef, Class... classesInJar)
        throws IOException, InterruptedException {
        return preloadJar(UserRequest.getClassLoader(classLoaderRef), classesInJar);
    }

    public boolean preloadJar(ClassLoader local, Class... classesInJar) throws IOException, InterruptedException {
        URL[] jars = new URL[classesInJar.length];
        for (int i = 0; i < classesInJar.length; i++) {
            jars[i] = Which.jarFile(classesInJar[i]).toURI().toURL();
        }
        return call(new PreloadJarTask(jars, local));
    }

    public boolean preloadJar(ClassLoader local, URL... jars) throws IOException, InterruptedException {
        return call(new PreloadJarTask(jars, local));
    }

    PipeWindow getPipeWindow(int oid) {
        synchronized (pipeWindows) {
            Key k = new Key(oid);
            WeakReference<PipeWindow> v = pipeWindows.get(k);
            if (v != null) {
                PipeWindow w = v.get();
                if (w != null) {
                    return w;
                }
            }
            PipeWindow w;
            if (remoteCapability.supportsPipeThrottling()) {
                w = new Real(k, PIPE_WINDOW_SIZE);
            } else {
                w = new PipeWindow.Fake();
            }
            pipeWindows.put(k, new WeakReference<PipeWindow>(w));
            return w;
        }
    }


    /**
     * {@inheritDoc}
     */
    public <V, T extends Throwable>
    V call(Callable<V, T> callable) throws IOException, T, InterruptedException {
        UserRequest<V, T> request = null;
        try {
            request = new UserRequest<V, T>(this, callable);
            UserResponse<V, T> r = request.call(this);
            return r.retrieve(this, UserRequest.getClassLoader(callable));

            // re-wrap the exception so that we can capture the stack trace of the caller.
        } catch (ClassNotFoundException e) {
            IOException x = new IOException("Remote call on " + name + " failed");
            x.initCause(e);
            throw x;
        } catch (Error e) {
            IOException x = new IOException("Remote call on " + name + " failed");
            x.initCause(e);
            throw x;
        } finally {
            // since this is synchronous operation, when the round trip is over
            // we assume all the exported objects are out of scope.
            // (that is, the operation shouldn't spawn a new thread or altter
            // global state in the remote system.
            if (request != null) {
                request.releaseExports();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public <V, T extends Throwable>
    Future<V> callAsync(final Callable<V, T> callable) throws IOException {
        final Future<UserResponse<V, T>> f = new UserRequest<V, T>(this, callable).callAsync(this);
        return new FutureAdapter<V, UserResponse<V, T>>(f) {
            protected V adapt(UserResponse<V, T> r) throws ExecutionException {
                try {
                    return r.retrieve(Channel.this, UserRequest.getClassLoader(callable));
                } catch (Throwable t) {// really means catch(T t)
                    throw new ExecutionException(t);
                }
            }
        };
    }

    /*
     * This method provides a mean to flush the I/O pipe associated with this
     * channel. Useful when the process associated with the channel is terminating
     * but the pipe might still transmitting data.
     * See http://issues.hudson-ci.org/browse/HUDSON-7809
     */
    public void flushPipe() throws IOException, InterruptedException {
        // The solution is to create no-op dummy RemotePipeWriter  callable and submit
        // to the channel synchronously.


        try {
            pipeWriter.submit(new Runnable() {

                public void run() {
                    // Do nothing, just a dummy runnable just to flush
                    // this side of the Pipe
                }
            }).get();
        } catch (ExecutionException exc) {
            throw new AssertionError(exc);
        }
        // Do not use anonymous class, other wise whole class gets marshalled over pipe and
        // the channel class is not serializable.
        call(new DummyRemotePipeWriterCallable());
    }

    public static class DummyRemotePipeWriterCallable implements Callable<Object, InterruptedException>, Serializable {

        public Object call() throws InterruptedException {
            try {
                return Channel.current().pipeWriter.submit(new Runnable() {

                    public void run() {
                        // Do nothing, just a dummy runnable just to flush
                        // other side of the Pipe
                    }
                }).get();
            } catch (ExecutionException exc) {
                throw new AssertionError(exc);
            }
        }
    }

    ;


    /**
     * Aborts the connection in response to an error.
     *
     * @param e The error that caused the connection to be aborted. Never null.
     */
    protected synchronized void terminate(IOException e) {
        if (e == null) {
            throw new IllegalArgumentException();
        }
        outClosed = inClosed = e;
        try {
            synchronized (pendingCalls) {
                for (Request<?, ?> req : pendingCalls.values()) {
                    req.abort(e);
                }
                pendingCalls.clear();
            }
            synchronized (executingCalls) {
                for (Request<?, ?> r : executingCalls.values()) {
                    java.util.concurrent.Future<?> f = r.future;
                    if (f != null) {
                        f.cancel(true);
                    }
                }
                executingCalls.clear();
            }
        } finally {
            notifyAll();

            if (e instanceof OrderlyShutdown) {
                e = null;
            }
            for (Listener l : listeners.toArray(new Listener[listeners.size()])) {
                l.onClosed(this, e);
            }
        }
    }

    /**
     * Registers a new {@link Listener}.
     *
     * @see #removeListener(Listener)
     */
    public void addListener(Listener l) {
        listeners.add(l);
    }

    /**
     * Removes a listener.
     *
     * @return false if the given listener has not been registered to begin with.
     */
    public boolean removeListener(Listener l) {
        return listeners.remove(l);
    }

    /**
     * Waits for this {@link Channel} to be closed down.
     * <p/>
     * The close-down of a {@link Channel} might be initiated locally or remotely.
     *
     * @throws InterruptedException If the current thread is interrupted while waiting for the completion.
     */
    public synchronized void join() throws InterruptedException {
        while (inClosed == null || outClosed == null) {
            wait();
        }
    }

    /**
     * If the receiving end of the channel is closed (that is, if we are guaranteed to receive nothing further),
     * this method returns true.
     */
    /*package*/ boolean isInClosed() {
        return inClosed != null;
    }

    /**
     * Waits for this {@link Channel} to be closed down, but only up the given milliseconds.
     *
     * @throws InterruptedException If the current thread is interrupted while waiting for the completion.
     * @since 1.299
     */
    public synchronized void join(long timeout) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeout && (inClosed == null || outClosed == null)) {
            wait(timeout + start - System.currentTimeMillis());
        }
    }

    /**
     * Notifies the remote peer that we are closing down.
     * <p/>
     * Execution of this command also triggers the {@link ReaderThread} to shut down
     * and quit. The {@link CloseCommand} is always the last command to be sent on
     * {@link ObjectOutputStream}, and it's the last command to be read.
     */
    private static final class CloseCommand extends Command {
        protected void execute(Channel channel) {
            try {
                channel.close();
                channel.terminate(new OrderlyShutdown(createdAt));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "close command failed on " + channel.name, e);
                logger.log(Level.INFO, "close command created at", createdAt);
            }
        }

        @Override
        public String toString() {
            return "close";
        }
    }

    /**
     * Signals the orderly shutdown of the channel, but captures
     * where the termination was initiated as a nested exception.
     */
    private static final class OrderlyShutdown extends IOException {
        private OrderlyShutdown(Throwable cause) {
            super(cause.getMessage());
            initCause(cause);
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Resets all the performance counters.
     */
    public void resetPerformanceCounters() {
        classLoadingCount.set(0);
        classLoadingTime.set(0);
        resourceLoadingCount.set(0);
        resourceLoadingTime.set(0);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void close() throws IOException {
        if (outClosed != null) {
            return;  // already closed
        }

        send(new CloseCommand());
        outClosed
            = new IOException();   // last command sent. no further command allowed. lock guarantees that no command will slip inbetween
        try {
            oos.close();
        } catch (IOException e) {
            // there's a race condition here.
            // the remote peer might have already responded to the close command
            // and closed the connection, in which case our close invocation
            // could fail with errors like
            // "java.io.IOException: The pipe is being closed"
            // so let's ignore this error.
        }

        // termination is done by CloseCommand when we received it.
    }

    /**
     * Gets the application specific property set by {@link #setProperty(Object, Object)}.
     * These properties are also accessible from the remote channel via {@link #getRemoteProperty(Object)}.
     * <p/>
     * <p/>
     * This mechanism can be used for one side to discover contextual objects created by the other JVM
     * (as opposed to executing {@link Callable}, which cannot have any reference to the context
     * of the remote {@link Channel}.
     */
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    public <T> T getProperty(ChannelProperty<T> key) {
        return key.type.cast(properties.get(key));
    }

    /**
     * Works like {@link #getProperty(Object)} but wait until some value is set by someone.
     */
    public Object waitForProperty(Object key) throws InterruptedException {
        synchronized (properties) {
            while (true) {
                Object v = properties.get(key);
                if (v != null) {
                    return v;
                }
                properties.wait();
            }
        }
    }

    /**
     * Sets the property value on this side of the channel.
     *
     * @see #getProperty(Object)
     */
    public Object setProperty(Object key, Object value) {
        synchronized (properties) {
            Object old = value != null ? properties.put(key, value) : properties.remove(key);
            properties.notifyAll();
            return old;
        }
    }

    public Object getRemoteProperty(Object key) {
        return remoteChannel.getProperty(key);
    }

    public Object waitForRemoteProperty(Object key) throws InterruptedException {
        return remoteChannel.waitForProperty(key);
    }

    /**
     * Starts a local to remote port forwarding (the equivalent of "ssh -L").
     *
     * @param recvPort The port on this local machine that we'll listen to. 0 to let
     * OS pick a random available port. If you specify 0, use
     * {@link ListeningPort#getPort()} to figure out the actual assigned port.
     * @param forwardHost The remote host that the connection will be forwarded to.
     * Connection to this host will be made from the other JVM that
     * this {@link Channel} represents.
     * @param forwardPort The remote port that the connection will be forwarded to.
     * @return
     */
    public ListeningPort createLocalToRemotePortForwarding(int recvPort, String forwardHost, int forwardPort)
        throws IOException, InterruptedException {
        return new PortForwarder(recvPort,
            ForwarderFactory.create(this, forwardHost, forwardPort));
    }

    /**
     * Starts a remote to local port forwarding (the equivalent of "ssh -R").
     *
     * @param recvPort The port on the remote JVM (represented by this {@link Channel})
     * that we'll listen to. 0 to let
     * OS pick a random available port. If you specify 0, use
     * {@link ListeningPort#getPort()} to figure out the actual assigned port.
     * @param forwardHost The remote host that the connection will be forwarded to.
     * Connection to this host will be made from this JVM.
     * @param forwardPort The remote port that the connection will be forwarded to.
     * @return
     */
    public ListeningPort createRemoteToLocalPortForwarding(int recvPort, String forwardHost, int forwardPort)
        throws IOException, InterruptedException {
        return PortForwarder.create(this, recvPort,
            ForwarderFactory.create(forwardHost, forwardPort));
    }

    @Override
    public String toString() {
        return super.toString() + ":" + name;
    }

    /**
     * Dumps the list of exported objects and their allocation traces to the given output.
     */
    public void dumpExportTable(PrintWriter w) throws IOException {
        exportedObjects.dump(w);
    }

    public ExportList startExportRecording() {
        return exportedObjects.startRecording();
    }

    /**
     * @see #lastHeard
     */
    public long getLastHeard() {
        return lastHeard;
    }

    private final class ReaderThread extends Thread {
        public ReaderThread(String name) {
            super("Channel reader thread: " + name);
        }

        @Override
        public void run() {
            Command cmd = null;
            try {
                while (inClosed == null) {
                    try {
                        Channel old = Channel.setCurrent(Channel.this);
                        try {
                            cmd = (Command) ois.readObject();
                            lastHeard = System.currentTimeMillis();
                        } finally {
                            Channel.setCurrent(old);
                        }
                    } catch (EOFException e) {
                        IOException ioe = new IOException("Unexpected termination of the channel");
                        ioe.initCause(e);
                        throw ioe;
                    } catch (ClassNotFoundException e) {
                        logger.log(Level.SEVERE, "Unable to read a command (channel " + name + ")", e);
                    }
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Received " + cmd);
                    }
                    try {
                        cmd.execute(Channel.this);
                    } catch (Throwable t) {
                        logger.log(Level.SEVERE, "Failed to execute command " + cmd + " (channel " + name + ")", t);
                        logger.log(Level.SEVERE, "This command is created here", cmd.createdAt);
                    }
                }
                ois.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "I/O error in channel " + name, e);
                terminate(e);
            } finally {
                pipeWriter.shutdown();
            }
        }
    }

    /*package*/
    static Channel setCurrent(Channel channel) {
        Channel old = CURRENT.get();
        CURRENT.set(channel);
        return old;
    }

    /**
     * This method can be invoked during the serialization/deserialization of
     * objects when they are transferred to the remote {@link Channel},
     * as well as during {@link Callable#call()} is invoked.
     *
     * @return null
     *         if the calling thread is not performing serialization.
     */
    public static Channel current() {
        return CURRENT.get();
    }

    /**
     * Remembers the current "channel" associated for this thread.
     */
    private static final ThreadLocal<Channel> CURRENT = new ThreadLocal<Channel>();

    private static final Logger logger = Logger.getLogger(Channel.class.getName());

    public static final int PIPE_WINDOW_SIZE = Integer.getInteger(Channel.class + ".pipeWindowSize", 128 * 1024);

//    static {
//        ConsoleHandler h = new ConsoleHandler();
//        h.setFormatter(new Formatter(){
//            public synchronized String format(LogRecord record) {
//                StringBuilder sb = new StringBuilder();
//                sb.append((record.getMillis()%100000)+100000);
//                sb.append(" ");
//                if (record.getSourceClassName() != null) {
//                    sb.append(record.getSourceClassName());
//                } else {
//                    sb.append(record.getLoggerName());
//                }
//                if (record.getSourceMethodName() != null) {
//                    sb.append(" ");
//                    sb.append(record.getSourceMethodName());
//                }
//                sb.append('\n');
//                String message = formatMessage(record);
//                sb.append(record.getLevel().getLocalizedName());
//                sb.append(": ");
//                sb.append(message);
//                sb.append('\n');
//                if (record.getThrown() != null) {
//                    try {
//                        StringWriter sw = new StringWriter();
//                        PrintWriter pw = new PrintWriter(sw);
//                        record.getThrown().printStackTrace(pw);
//                        pw.close();
//                        sb.append(sw.toString());
//                    } catch (Exception ex) {
//                    }
//                }
//                return sb.toString();
//            }
//        });
//        h.setLevel(Level.FINE);
//        logger.addHandler(h);
//        logger.setLevel(Level.FINE);
//    }
}
