/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.maven.plugin.builder.internal;

import org.hudsonci.maven.eventspy.common.Callback;
import org.hudsonci.maven.eventspy.common.CallbackManager;
import hudson.FilePath;
import hudson.remoting.Callable;

import org.hudsonci.maven.plugin.builder.internal.invoker.CallbackCloseAwareHandler;
import org.hudsonci.maven.plugin.builder.internal.invoker.Invoker;
import org.hudsonci.maven.plugin.builder.internal.invoker.InvokerImpl;
import org.hudsonci.maven.plugin.builder.internal.invoker.ObjectLocalHandler;
import org.hudsonci.maven.plugin.builder.internal.invoker.RecordingHandler;
import org.hudsonci.maven.plugin.builder.internal.invoker.RemoteInvokeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Starts the event processing on the remote node.
 * 
 * Call will return after all events have been processed.
 * This is required to be executing on the remote node to allow the callback communication to remain open.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class StartProcessing
    implements Callable<Object,Exception>
{
    private static final Logger log = LoggerFactory.getLogger(StartProcessing.class);
    
    private final Invoker invoker;

    private File recordFile;

    public StartProcessing(final Callback callback) {
        this.invoker = new InvokerImpl(checkNotNull(callback));
    }

    public void recordInvocationsTo(final FilePath recordFile) {
        this.recordFile = new File(checkNotNull(recordFile).getRemote());
    }

    /**
     * Initializes {@link Callback} via {@link CallbackManager}.
     * Sets up invoke handlers to deal with {@link Callback#close()}.
     */
    public Object call() throws Exception {
        log.debug("Preparing to start processing");

        final Object lock = new Object();

        // Setup the invocation handler chain
        InvocationHandler chain;

        // Does the remoting
        chain = new RemoteInvokeHandler(invoker);

        // Handles shutting down
        chain = new CallbackCloseAwareHandler(chain)
        {
            @Override
            protected void onClose() {
                // notify to stop processing
                log.debug("Close invoked; notifying to stop processing");
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };

        RecordingHandler recorder = null;
        if (recordFile != null) {
            // Records invocations to a file
            chain = recorder = new RecordingHandler(chain, recordFile);
        }

        // Handles Object.*() calls locally
        chain = new ObjectLocalHandler(chain);

        // Setup the Callback proxy to handle local and remote invocations
        Callback callback = (Callback)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] { Callback.class },
                chain);

        log.debug("Prepared callback: {}", callback);

        // Install the callback, this will trigger Maven to begin/continue execution
        CallbackManager.set(callback);

        log.debug("Started");

        // Wait to allow processing
        synchronized (lock) {
            lock.wait();
        }

        // Close the recorder
        if (recorder != null) {
            recorder.close();
        }

        log.debug("Processing stopped");

        return null;
    }
}
