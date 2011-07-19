/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.maven.plugin.builder.internal.invoker;

import org.eclipse.hudson.utils.io.Closer;
import org.eclipse.hudson.maven.model.InvocationDTO;
import com.thoughtworks.xstream.XStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handler which records invocations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class RecordingHandler
    extends DelegatingInvocationHandler
{
    public static final String INVOCATION_STREAM = "invocation-stream";

    private final XStream xs;

    private final File file;

    private final Writer writer;

    private final ObjectOutputStream output;

    public RecordingHandler(final InvocationHandler delegate, final File file) throws IOException {
        super(delegate);

        assert file != null;
        this.file = file;
        log.debug("Recording invocations to: {}", file);

        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        xs = new XStream();
        xs.autodetectAnnotations(true);
        writer = new BufferedWriter(new FileWriter(file));
        output = xs.createObjectOutputStream(writer, INVOCATION_STREAM);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        assert method != null;

        InvocationDTO.Result result = new InvocationDTO.Result();
        InvocationDTO invocation = invocationOf(method, args).withResult(result);

        Object value;
        try {
            value = getDelegate().invoke(proxy, method, args);
            result.withException(false).withValue(value);
        }
        catch (Throwable t) {
            result.withException(true).withValue(t);
            throw t;
        }
        finally {
            try {
                synchronized (output) {
                    output.writeObject(invocation);
                }
            }
            catch (IOException e) {
                log.error("Write object failed", e);
            }
        }

        return value;
    }

    private InvocationDTO invocationOf(final Method method, final Object[] args) {
        assert method != null;
        // args may be null

        InvocationDTO.Method imethod = new InvocationDTO.Method().withName(method.getName());
        for (Class type : method.getParameterTypes()) {
            imethod.getTypes().add(type.getName());
        }

        return new InvocationDTO().withMethod(imethod).withArgs(args);
    }

    // TODO: Add a class to read a recorded session and translate into invokes on a Callback instance

    public void close() {
        Closer.close(output);
    }
}
