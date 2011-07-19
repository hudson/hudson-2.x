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
*    Kohsuke Kawaguchi, InfraDNA, Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.remoting;

import hudson.remoting.Channel.Mode;
import junit.framework.Assert;

import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * Hides the logic of starting/stopping a channel for test.
 *
 * @author Kohsuke Kawaguchi
 */
interface ChannelRunner {

    Channel start() throws Exception;

    void stop(Channel channel) throws Exception;

    String getName();

    /**
     * Runs a channel in the same JVM.
     */
    static class InProcess implements ChannelRunner {

        private ExecutorService executor;
        /**
         * failure occurred in the other {@link Channel}.
         */
        private Exception failure;

        public Channel start() throws Exception {
            final FastPipedInputStream in1 = new FastPipedInputStream();
            final FastPipedOutputStream out1 = new FastPipedOutputStream(in1);

            final FastPipedInputStream in2 = new FastPipedInputStream();
            final FastPipedOutputStream out2 = new FastPipedOutputStream(in2);

            executor = Executors.newCachedThreadPool(new ThreadFactory() {

                private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

                public Thread newThread(final Runnable r) {
                    System.out.println("Creating New Thread");
                    return defaultFactory.newThread(new Runnable() {

                        public void run() {
                            r.run();
                        }
                    });
                }
            });

            //executor = Executors.newCachedThreadPool();

            Thread t = new Thread("south bridge runner") {

                public void run() {
                    try {
                        Channel s = new Channel("south", executor, Mode.BINARY, in2, out1, null, false, createCapability());
                        s.join();
                        System.out.println("south completed");
                    } catch (IOException e) {
                        e.printStackTrace();
                        failure = e;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        failure = e;
                    }
                }
            };
            t.start();

            return new Channel("north", executor, Mode.BINARY, in1, out2, null, false, createCapability());
        }

        public void stop(Channel channel) throws Exception {
            channel.close();

            System.out.println("north completed");

            executor.shutdown();

            if (failure != null) {
                throw failure;  // report a failure in the south side
            }
        }

        public String getName() {
            return "local";
        }

        protected Capability createCapability() {
            return new Capability();
        }
    }

    static class InProcessCompatibilityMode extends InProcess {

        public String getName() {
            return "local-compatibility";
        }

        @Override
        protected Capability createCapability() {
            return Capability.NONE;
        }
    }

    /**
     * Runs a channel in a separate JVM by launching a new JVM.
     */
    static class Fork implements ChannelRunner {

        private Process proc;
        private ExecutorService executor;
        private Copier copier;

        public Channel start() throws Exception {
            System.out.println("forking a new process");
            // proc = Runtime.getRuntime().exec("java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000 hudson.remoting.Launcher");

            System.out.println(getClasspath());
            proc = Runtime.getRuntime().exec(new String[]{"java", "-cp", getClasspath(), "hudson.remoting.Launcher"});

            copier = new Copier("copier", proc.getErrorStream(), System.out);
            copier.start();

            executor = Executors.newCachedThreadPool();
            OutputStream out = proc.getOutputStream();
            if (RECORD_OUTPUT) {
                File f = File.createTempFile("remoting", ".log");
                System.out.println("Recording to " + f);
                out = new TeeOutputStream(out, new FileOutputStream(f));
            }
            return new Channel("north", executor, proc.getInputStream(), out);
        }

        public void stop(Channel channel) throws Exception {
            channel.close();
            channel.join(10 * 1000);

//            System.out.println("north completed");

            executor.shutdown();

            copier.join();
            int r = proc.waitFor();
//            System.out.println("south completed");

            Assert.assertEquals("exit code should have been 0", 0, r);
        }

        public String getName() {
            return "fork";
        }

        public String getClasspath() {
            // this assumes we run in Maven
            StringBuilder buf = new StringBuilder();
            URLClassLoader ucl = (URLClassLoader) getClass().getClassLoader();
            for (URL url : ucl.getURLs()) {
                if (buf.length() > 0) {
                    buf.append(File.pathSeparatorChar);
                }
                buf.append(FileUtils.toFile(url)); // assume all of them are file URLs
            }
            return buf.toString();
        }
        /**
         * Record the communication to the remote node. Used during debugging.
         */
        private static boolean RECORD_OUTPUT = false;
    }
}
